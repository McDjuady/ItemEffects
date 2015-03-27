/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.effect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

/**
 *
 * @author Max
 */
public class EffectData implements Cloneable{

    private final Map<String, Object> data;
    private final Map<String, EffectDataCombiner> combiners;

    public EffectData(EffectDataHelper[] options, String effectInfo, ConfigurationSection defaultConfig) {
        data = new HashMap<>();
        combiners = new HashMap<>();
        for (EffectDataHelper option : options) {
            Matcher matcher = option.matcher(effectInfo);
            if (matcher.find()) {
                String group = matcher.group();
                String value = effectInfo.substring(matcher.start() + group.indexOf("=") + 1, matcher.start() + group.length() - 1); //extract the data, keep the Case
                data.put(option.key().toLowerCase(), option.cast(value));
            } else if (defaultConfig != null && defaultConfig.contains(option.key())) { //use defaultConfig
                data.put(option.key().toLowerCase(), option.cast(defaultConfig.getString(option.key())));
            } else { //use predefined default
                data.put(option.key().toLowerCase(), option.cast(option.value()));
            }
            combiners.put(option.key().toLowerCase(), option.getCombiner());
        }
    }

    public EffectData(Map<String, Object> data, Map<String, EffectDataCombiner> combiners) {
        this.data = data;
        this.combiners = combiners;
    }

    public Object get(String key) {
        return data.get(key.toLowerCase());
    }

    public boolean contains(String key) {
        return data.containsKey(key.toLowerCase());
    }

    public Double getDouble(String key) {
        Object o = get(key);
        return o instanceof Number ? ((Number) o).doubleValue() : 0;
    }

    public Integer getInt(String key) {
        Object o = get(key);
        return o instanceof Number ? ((Number) o).intValue() : 0;
    }

    public Float getFloat(String key) {
        Object o = get(key);
        return o instanceof Number ? ((Number) o).floatValue() : 0;
    }

    public Boolean getBoolean(String key) {
        Object o = get(key);
        return o instanceof Boolean ? (Boolean) o : Boolean.valueOf(String.valueOf(o));
    }

    public String getString(String key) {
        return String.valueOf(get(key));
    }

    public boolean isSimmilar(EffectData otherData) {
        return this.getClass().equals(otherData.getClass()) && otherData.data.keySet().equals(data.keySet());
    }

    public void combine(EffectData otherData) {
        for (String key : data.keySet()) {
            data.put(key, combiners.get(key).combine(get(key), otherData.get(key)));
        }
    }

    public void remove(EffectData otherData) {
        for (String key : data.keySet()) {
            data.put(key, combiners.get(key).remove(get(key), otherData.get(key)));
        }
    }

    @Override
    public EffectData clone() {
        try {
            Constructor<? extends EffectData> constructor = this.getClass().getConstructor(Map.class, Map.class);
            return constructor.newInstance(new HashMap<>(data), new HashMap<>(combiners));
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to find clone constructor for EffectDataClass " + this.getClass().getSimpleName() + "! Using default EffectData", ex);
            return new EffectData(data, combiners);
        }
    }

}
