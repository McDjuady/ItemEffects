/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects;

import com.googlemail.mcdjuady.itemeffects.effect.Effect;
import com.googlemail.mcdjuady.itemeffects.effect.PlayerEffects;
import com.googlemail.mcdjuady.itemeffects.effect.EffectData;
import com.googlemail.mcdjuady.itemeffects.effect.EffectHandler;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Max
 */
public class EffectManager {

    private class EffectListenerMethod {

        private final Method method;

        public EffectListenerMethod(Method method) {
            this.method = method;
        }

        public void invoke(Effect effect, EffectData data, Player player, Event event) {
            try {
                Bukkit.getLogger().log(Level.INFO, "Invoke {0}", method.getName());
                method.invoke(effect, data, player, event);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Bukkit.getLogger().log(Level.INFO, "Failed to call method {0} with {1} for {2}", new Object[]{method.getName(), event.getClass().getName(), effect.getEffectName()});
                Bukkit.getLogger().log(Level.INFO, null, ex);
            }
        }
    }

    private class EffectInfo {

        private final Class<? extends Effect> effectClass;
        private final Constructor<? extends Effect> defaultConstructor;
        private final Constructor<? extends Effect> enchantConstructor;
        private final ConfigurationSection defaultSection;

        public EffectInfo(Class<? extends Effect> effectClass, ConfigurationSection defaultSection) throws NoSuchMethodException {
            this.effectClass = effectClass;
            this.defaultSection = defaultSection;
            this.defaultConstructor = effectClass.getConstructor(ConfigurationSection.class, ItemStack.class, String.class);
            this.enchantConstructor = effectClass.getConstructor(ConfigurationSection.class, ItemStack.class, String[].class);
        }

        public Effect create(ItemStack item, String info) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            return defaultConstructor.newInstance(defaultSection, item, info);
        }

        public Effect enchant(ItemStack item, String... options) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            return enchantConstructor.newInstance(defaultSection, item, options);
        }
    }

    private final Map<String, Class<? extends Effect>> effectClasses;
    private final Map<Class<? extends Event>, Map<Class<? extends Effect>, List<EffectListenerMethod>>> eventListeners;
    private final Map<String, EffectInfo> effects;
    private final Map<UUID, PlayerEffects> playerEffects;

    public EffectManager() {
        effects = new HashMap<>();
        effectClasses = new HashMap<>();
        eventListeners = new HashMap<>();
        playerEffects = new HashMap<>();
    }

    public Effect createEffect(String effectName, ItemStack item, String lore) {
        EffectInfo info = effects.get(effectName);
        if (info == null) {
            Bukkit.getLogger().info("Null info");
            return null;
        }
        try {
            return info.create(item, lore);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to create Effect " + effectName + "! Args: [" + item.toString() + ", " + lore + "]", ex);
            return null;
        }
    }

    public Effect enchant(String effectName, ItemStack item, String... args) {
        EffectInfo info = effects.get(effectName);
        if (info == null) {
            Bukkit.getLogger().log(Level.INFO, "Invalid Effect {0}", effectName);
            return null;
        }
        try {
            return info.enchant(item, args);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to enchant wit Effect " + effectName + "! Args: [" + item.toString() + ", " + Arrays.toString(args) + "]", ex);
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
            if (params.length != 3 || !EffectData.class.isAssignableFrom(params[0]) || !Player.class.isAssignableFrom(params[1]) || !Event.class.isAssignableFrom(params[2])) {
                continue; //wrong parameters
            }
            for (Class<? extends Event> eventClass : annotation.value()) {
                if (!params[2].isAssignableFrom(eventClass)) {
                    Bukkit.getLogger().log(Level.INFO, "EventClass {0} isn't Assignable for {1}", new Object[]{eventClass.getName(), params[2].getName()});
                    continue;
                }
                Map<Class<? extends Effect>, List<EffectListenerMethod>> listenerMap = eventListeners.get(eventClass);
                if (listenerMap == null) {
                    listenerMap = new HashMap<>();
                    eventListeners.put(eventClass, listenerMap);
                }
                List<EffectListenerMethod> list = listenerMap.get(effectClass);
                if (list == null) {
                    list = new ArrayList<>();
                    listenerMap.put(effectClass, list);
                }
                list.add(new EffectListenerMethod(method));
            }
        }
    }

    private void registerEffect(ConfigurationSection section) {
        String effectId = section.getString("EffectId");
        Class<? extends Effect> effectClass = effectClasses.get(effectId);
        if (effectClass == null) {
            Bukkit.getLogger().log(Level.WARNING, "Invalid EffectId {0} in {1}", new Object[]{effectId, section.getName()});
        }
        try {
            EffectInfo info = new EffectInfo(effectClass, section);
            Bukkit.getLogger().log(Level.INFO, "Resgistered EffectConfig {0} for effect {1}", new Object[]{section.getName(), effectId});
            effects.put(section.getName(), info);
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
        Map<Class<? extends Effect>, List<EffectListenerMethod>> listeners = eventListeners.get(event.getClass());
        if (listeners == null) {
            return;
        }
        for (Entry<Class<? extends Effect>, List<EffectListenerMethod>> entry : listeners.entrySet()) {
            List<Effect> effectList = effects.getEffectsForClass(entry.getKey());
            if (effectList == null || effectList.isEmpty()) {
                continue;
            }
            for (Effect effect : effectList) {
                EffectData data = effect.isGlobal() ? effects.getGlobalData(effect) : effect.getEffectData();
                for (EffectListenerMethod method : entry.getValue()) {
                    method.invoke(effect, data, effects.getPlayer(), event);
                }
            }
        }
    }

    //fire for a specific event
    public void fireEvent(PlayerEffects effects, Effect effect, Event event) {
        Bukkit.getLogger().log(Level.INFO, "Event {0} for {1}", new Object[]{event.getEventName(), effect.getEffectName()});
        Map<Class<? extends Effect>, List<EffectListenerMethod>> listeners = eventListeners.get(event.getClass());
        if (listeners == null) {
            Bukkit.getLogger().info("nullListeners");
            return;
        }
        List<EffectListenerMethod> methods = listeners.get(effect.getClass());
        if (methods == null) {
            Bukkit.getLogger().info("nullMethods");
            return;
        }
        for (EffectListenerMethod method : methods) {
            method.invoke(effect, effect.getEffectData(), effects.getPlayer(), event);
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
}
