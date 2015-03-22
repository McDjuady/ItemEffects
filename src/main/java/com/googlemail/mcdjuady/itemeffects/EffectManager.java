/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
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

/**
 *
 * @author Max
 */
public class EffectManager {

    public class EffectMethodPair {

        private final Effect effect;
        private final Method method;

        public EffectMethodPair(Effect effect, Method method) {
            this.effect = effect;
            this.method = method;
        }

        /**
         * @return the effect
         */
        public Effect getEffect() {
            return effect;
        }

        /**
         * @return the method
         */
        public Method getMethod() {
            return method;
        }
    }

    private class EffectListenerMethod {

        private final Method method;

        public EffectListenerMethod(Method method) {
            this.method = method;
        }

        public void invoke(Effect effect, EffectData data, Player player, Event event) {
            try {
                method.invoke(effect, data, player, event);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Bukkit.getLogger().log(Level.INFO, "Failed to call method {0} with {1} for {2}", new Object[]{method.getName(), event.getClass().getName(), effect.getName()});
                Bukkit.getLogger().log(Level.INFO, null, ex);
            }
        }
    }

    private final Map<Class<? extends Event>, List<EffectMethodPair>> listeners;
    private final Map<String, Class<? extends Effect>> effectClasses;
    private final Map<Class<? extends Effect>, Map<Class<? extends Event>, List<EffectListenerMethod>>> effectListeners;
    private final Map<String, Effect> effects;
    private final Map<UUID, ActiveEffects> playerEffects;

    public EffectManager() {
        listeners = new HashMap<>();
        effects = new HashMap<>();
        playerEffects = new HashMap<>();
        effectClasses = new HashMap<>();
        effectListeners = new HashMap<>();
    }

    public Effect getEffect(String name) {
        return effects.get(name);
    }

    public void registerEffectClass(String id, Class<? extends Effect> effectClass) {
        if (effectClasses.containsKey(id)) {
            return; //TODO throw exception?
        }
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
            Map<Class<? extends Event>,List<EffectListenerMethod>> map = effectListeners.get(effectClass);
            if (map == null) {
                map = new HashMap<>();
                effectListeners.put(effectClass, map);
            }
            for (Class<? extends Event> eventClass : annotation.value()) {
                if (!params[2].isAssignableFrom(eventClass)) {
                    Bukkit.getLogger().log(Level.INFO, "EventClass {0} isn't Assignable for {1}", new Object[]{eventClass.getName(), params[2].getName()});
                    continue;
                }
                List<EffectListenerMethod> list = map.get(eventClass);
                if (list == null) {
                    list = new ArrayList<>();
                    map.put(eventClass, list);
                }
                list.add(new EffectListenerMethod(method));
            }
        }
    }
    
    public Effect createEffect(ConfigurationSection section) {
        if (!section.contains("EffectId")) {
            return null;
        }
        String id = section.getString("EffectId");
        Class<? extends Effect> effectClass = effectClasses.get(id);
        if (effectClass == null) {
            return null;
        }
        
        try {
            Constructor<? extends Effect> constructor = effectClass.getConstructor(ConfigurationSection.class);
            return constructor.newInstance(section);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to create constructor for EffectID \"{0}\" with class {1}", new Object[]{id, effectClass.getName()});
            Bukkit.getLogger().log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public void createEffects(ConfigurationSection section) {
        for (String key : section.getKeys(false)) {
            ConfigurationSection effectInfo = section.getConfigurationSection(key);
            Effect effect = createEffect(effectInfo);
            if (effect != null) {
                effects.put(effect.getName(), effect);
            }
        }
    }
    
    public void registerEffect(Effect effect) {
        String effectName = effect.getName();
        if (effects.containsKey(effectName)) {
            return; //TODO throw exception?
        }
        effects.put(effectName, effect);
        Method[] methods = effect.getClass().getMethods();
        for (Method method : methods) {
            EffectHandler annotation = method.getAnnotation(EffectHandler.class);
            if (annotation == null) {
                Bukkit.getLogger().log(Level.INFO, "Method {0} doesn't have an annotation", method.getName());
                continue;
            }
            Class<?>[] params = method.getParameterTypes();
            if (params.length != 3 || !EffectData.class.isAssignableFrom(params[0]) || !Player.class.isAssignableFrom(params[1]) || !Event.class.isAssignableFrom(params[2])) {
                continue;
            }
            for (Class<? extends Event> eventClass : annotation.value()) {
                if (!params[2].isAssignableFrom(eventClass)) {
                    Bukkit.getLogger().log(Level.INFO, "EventClass {0} isn't Assignable for {1}", new Object[]{eventClass.getName(), params[2].getName()});
                    continue;
                }
                List<EffectMethodPair> eventList = listeners.get(eventClass);
                if (eventList == null) {
                    eventList = new ArrayList<>();
                    listeners.put(eventClass, eventList);
                }
                eventList.add(new EffectMethodPair(effect, method));
            }
        }
    }

    public void fireEvent2(ActiveEffects effectData, Event e) {
        if (effectData.isEmpty()) {
            return;
        }
        for (Entry<Effect,EffectData> entry : effectData.getEntrySet()) {
            Map<Class<? extends Event>,List<EffectListenerMethod>> effectMap = effectListeners.get(entry.getKey().getClass());
            List<EffectListenerMethod> methods = effectMap.get(e.getClass());
            for (EffectListenerMethod method : methods) {
                method.invoke(entry.getKey(), entry.getValue(), effectData.getPlayer(), e);
            }
        }
    }
    
    public void fireEvent(ActiveEffects effectData, Event e) {
        if (effectData.isEmpty()) {
            return;
        }
        List<EffectMethodPair> list = listeners.get(e.getClass());
        if (list == null) {
            Bukkit.getLogger().log(Level.INFO, "Null list for {0}", e.getClass().getName());
            return;
        }
        for (EffectMethodPair pair : list) {
            EffectData data = effectData.getEffectData(pair.getEffect());
            if (data == null) {
                Bukkit.getLogger().log(Level.INFO, "Skip {0}", pair.getEffect().getName());
                continue; //only fire for effects that are active
            }
            try {
                pair.getMethod().invoke(pair.getEffect(), effectData.getEffectData(pair.getEffect()), effectData.getPlayer(), e);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Bukkit.getLogger().log(Level.INFO, "Failed to call method {0} with {1} for {2}", new Object[]{pair.getMethod().getName(), e.getClass().getName(), pair.getEffect().getName()});
                Bukkit.getLogger().log(Level.INFO, null, ex);
            }
        }
    }

    public ActiveEffects getActiveEffects(Player player) {
        ActiveEffects activeEffects = playerEffects.get(player.getUniqueId());
        if (activeEffects == null) {
            activeEffects = new ActiveEffects(player);
            playerEffects.put(player.getUniqueId(), activeEffects);
        }
        return activeEffects;
    }

    public void onPlayerQuit(Player player) {
        playerEffects.remove(player.getUniqueId());
    }

    public boolean hasActiveEffects(Player player) {
        return !getActiveEffects(player).isEmpty();
    }
}
