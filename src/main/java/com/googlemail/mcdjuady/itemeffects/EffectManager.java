/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects;

import com.comphenix.protocol.events.ListenerPriority;
import com.googlemail.mcdjuady.itemeffects.effect.Effect;
import static com.googlemail.mcdjuady.itemeffects.effect.Effect.dataPattern;
import com.googlemail.mcdjuady.itemeffects.effect.EffectHandler;
import com.googlemail.mcdjuady.itemeffects.effect.PlayerEffects;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;

/**
 *
 * @author Max
 */
public class EffectManager {

    public final static int INHANDSLOT = -1;
    public final static int GLOBALSLOT = -2;

    private class RegisteredEffectListener {

        private final Class<? extends Effect> effectClass;
        private final Method method;
        private final boolean ignoreCanceled;

        public RegisteredEffectListener(Class<? extends Effect> effectClass, Method method, boolean ignoreCanceled) {
            this.effectClass = effectClass;
            this.method = method;
            this.ignoreCanceled = ignoreCanceled;
        }

        public void invoke(Effect effect, Event event) {
            try {
                if (!(event instanceof Cancellable) || !(ignoreCanceled && ((Cancellable) event).isCancelled())) {
                    method.invoke(effect, event);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Bukkit.getLogger().log(Level.INFO, "Failed to call method {0} with {1} for {2}", new Object[]{method.getName(), event.getClass().getName(), effect.getEffectName()});
                Bukkit.getLogger().log(Level.INFO, null, ex);
            }
        }

        public Class<? extends Effect> getEffectClass() {
            return effectClass;
        }
    }

    private class EffectInfo {

        private final Constructor<? extends Effect> defaultConstructor;
        private final ConfigurationSection defaultSection;

        public EffectInfo(Class<? extends Effect> effectClass, ConfigurationSection defaultSection) throws NoSuchMethodException {
            this.defaultSection = defaultSection;
            this.defaultConstructor = effectClass.getConstructor(ConfigurationSection.class, String.class, PlayerEffects.class, int.class);
        }

        public Effect create(String info, PlayerEffects effects, int slot) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            return defaultConstructor.newInstance(defaultSection, info, effects, slot);
        }

        public Effect create(ConfigurationSection configSection, String info, PlayerEffects effects, int slot) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            for (String key : defaultSection.getKeys(true)) {
                configSection.addDefault(key, defaultSection.get(key));
            }
            return defaultConstructor.newInstance(configSection, info, effects, slot);
        }

    }

    private final Map<String, Class<? extends Effect>> effectClasses;
    private final Map<Class<? extends Event>, Map<Integer, List<RegisteredEffectListener>>> priorityListeners;
    private final Map<String, EffectInfo> effects;
    private final Map<UUID, PlayerEffects> playerEffects;

    public EffectManager() {
        effects = new HashMap<>();
        effectClasses = new HashMap<>();
        playerEffects = new HashMap<>();
        priorityListeners = new HashMap<>();
    }

    public Effect createEffect(String effectName, String lore, PlayerEffects playerEffects, int slot) {
        EffectInfo info = effects.get(effectName.toLowerCase());
        if (info == null) {
            return null;
        }
        try {
            return info.create(lore, playerEffects, slot);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to create Effect " + effectName + "! Args: [" + lore + "]", ex);
            return null;
        }
    }

    public Effect createEffect(ConfigurationSection section, PlayerEffects playerEffects, int slot) {
        EffectInfo info = effects.get(section.getName().toLowerCase());
        if (info == null) {
            return null;
        }
        try {
            return info.create(section, null, playerEffects, slot);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to create Effect " + section.getName() + "! Args: [" + section.toString() + "]", ex);
            return null;
        }
    }

    public Effect enchant(String effectName, Player player, int slot, String... args) {
        return enchant(effectName, getPlayerEffects(player), slot, args);
    }

    public Effect enchant(String effectName, PlayerEffects playerEffects, int slot, String... args) {
        EffectInfo info = effects.get(effectName.toLowerCase());
        if (info == null) {
            Bukkit.getLogger().log(Level.INFO, "Invalid Effect {0}", effectName);
            return null;
        }
        StringBuilder effectInfo = new StringBuilder();
        for (String string : args) {
            if (dataPattern.matcher(string).matches()) {
                effectInfo.append(Effect.dataSperator).append(string);
            }
        }
        effectInfo.append("|");
        try {
            Effect effect = info.create(effectInfo.toString(), playerEffects, slot);
            effect.inscribe();
            return effect;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to enchant wit Effect " + effectName + "! Args: [" + Arrays.toString(args) + "]", ex);
            return null;
        }
    }

    public void registerEffectClass(String id, Class<? extends Effect> effectClass) {
        if (effectClasses.containsKey(id)) {
            return; //TODO throw exception?
        }
        Bukkit.getLogger().log(Level.INFO, "Registerning Effect {0} with class {1}", new Object[]{id, effectClass.getSimpleName()});
        effectClasses.put(id, effectClass);
        for (Method method : effectClass.getMethods()) {
            EffectHandler annotation = method.getAnnotation(EffectHandler.class);
            if (annotation == null) {
                continue; //skip non listener methods
            }
            Class<?>[] params = method.getParameterTypes();
            if (params.length != 1 || !Event.class.isAssignableFrom(params[0])) {
                continue; //wrong parameters
            }
            int priority = annotation.priority().getSlot();
            boolean ignoreCancelled = annotation.ignoreCancelled();
            Class<? extends Event>[] eventClasses = annotation.events();
            if (eventClasses.length == 0) { //if no events are set just listen for the one in the funciton
                eventClasses = (Class<? extends Event>[]) Array.newInstance(params[0].getClass(), 1);
                eventClasses[0] = (Class<? extends Event>) params[0];
            }
            for (Class<? extends Event> eventClass : eventClasses) {
                if (!params[0].isAssignableFrom(eventClass)) {
                    Bukkit.getLogger().log(Level.INFO, "EventClass {0} isn't Assignable for {1}", new Object[]{eventClass.getName(), params[2].getName()});
                    continue;
                }
                Map<Integer, List<RegisteredEffectListener>> listenerMap = priorityListeners.get(eventClass);
                if (listenerMap == null) {
                    listenerMap = new HashMap<>();
                    priorityListeners.put(eventClass, listenerMap);
                }
                List<RegisteredEffectListener> list = listenerMap.get(priority);
                if (list == null) {
                    list = new ArrayList<>();
                    listenerMap.put(priority, list);
                }
                list.add(new RegisteredEffectListener(effectClass, method, ignoreCancelled));
            }
        }
    }

    private void registerEffect(ConfigurationSection section) {
        String effectId = section.getString("EffectId");
        Class<? extends Effect> effectClass = effectClasses.get(effectId);
        if (effectClass == null) {
            Bukkit.getLogger().log(Level.WARNING, "Invalid EffectId {0} in {1}", new Object[]{effectId, section.getName()});
            return;
        }
        try {
            EffectInfo info = new EffectInfo(effectClass, section);
            Bukkit.getLogger().log(Level.INFO, "Resgistered EffectConfig {0} for effect {1}", new Object[]{section.getName(), effectId});
            effects.put(section.getName().toLowerCase(), info);
        } catch (NoSuchMethodException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to register effect " + section.getName() + "!", ex);
        }
    }

    public void registerEffects(ConfigurationSection section) {
        for (String key : section.getKeys(false)) {
            ConfigurationSection effectSection = section.getConfigurationSection(key);
            if (effectSection.contains("EffectId")) {
                registerEffect(effectSection);
            }
        }
    }

    public void fireEvent(PlayerEffects effects, Event event) {
        Map<Integer, List<RegisteredEffectListener>> listeners = priorityListeners.get(event.getClass());
        if (listeners == null) {
            return;
        }
        for (int i = 0; i < EventPriority.values().length; i++) {
            List<RegisteredEffectListener> list = listeners.get(i);
            if (list == null) {
                continue;
            }
            for (RegisteredEffectListener listener : list) {
                List<Effect> effectList = effects.getEffectsForClass(listener.getEffectClass());
                if (effectList == null) {
                    continue;
                }
                for (Effect effect : effectList) {
                    if (!effects.isDisabled() || effect.ignoresDisabled()) {
                        listener.invoke(effect, event);
                    }
                }
            }
        }
    }

    public void fireEvent(PlayerEffects effects, Event event, EventPriority priority) {
        fireEvent(effects, event, priority.getSlot());
    }
    
    public void fireEvent(PlayerEffects effects, Event event, int priority) {
        Map<Integer, List<RegisteredEffectListener>> listeners = priorityListeners.get(event.getClass());
        if (listeners == null) {
            return;
        }
        List<RegisteredEffectListener> list = listeners.get(priority);
        if (list == null) {
            return;
        }
        for (RegisteredEffectListener listener : list) {
            List<Effect> effectList = effects.getEffectsForClass(listener.getEffectClass());
            if (effectList == null) {
                continue;
            }
            for (Effect effect : effectList) {
                if (!effects.isDisabled() || effect.ignoresDisabled()) {
                    listener.invoke(effect, event);
                }
            }
        }
    }

    //fire for a specific effect
    public void fireEvent(PlayerEffects effects, Effect effect, Event event) {
        if (effects.isDisabled() && !effect.ignoresDisabled()) {
            return;
        }
        Map<Integer, List<RegisteredEffectListener>> newListeners = priorityListeners.get(event.getClass());
        if (newListeners == null) {
            return;
        }
        for (int i = 0; i < EventPriority.values().length; i++) {
            List<RegisteredEffectListener> list = newListeners.get(i);
            if (list == null) {
                continue;
            }
            for (RegisteredEffectListener listener : list) {
                if (listener.getEffectClass().equals(effect.getClass())) {
                    listener.invoke(effect, event);
                }
            }
        }
    }

    public PlayerEffects getPlayerEffects(Player player) {
        PlayerEffects pEffects = playerEffects.get(player.getUniqueId());
        if (pEffects == null) {
            pEffects = new PlayerEffects(player);
            playerEffects.put(player.getUniqueId(), pEffects);
        }
        return pEffects;
    }

    public void onPlayerJoin(Player player) {
        playerEffects.put(player.getUniqueId(), new PlayerEffects(player));
    }

    public void onPlayerQuit(Player player) {
        PlayerEffects pEffects = playerEffects.get(player.getUniqueId()); //can't remove quite yet
        if (pEffects != null) {
            pEffects.deactivateAll();
        }
        playerEffects.remove(player.getUniqueId());
    }

    public void onDisable() {
        for (PlayerEffects pEffects : playerEffects.values()) {
            pEffects.deactivateAll();
        }
    }
}
