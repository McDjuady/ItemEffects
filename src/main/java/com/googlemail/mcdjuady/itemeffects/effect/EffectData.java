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
import java.util.logging.Logger;
import java.util.regex.Matcher;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

/**
 *
 * @author Max
 */
public class EffectData {
    
    private final Map<String,Object> data;
    private final Map<String,EffectDataCombiner> combiners;
    
    public EffectData(EffectDataHelper[] options, String effectInfo, ConfigurationSection defaultConfig) {
        data = new HashMap<>();
        combiners = new HashMap<>();
        for (EffectDataHelper option : options) {
            Matcher matcher = option.matcher(effectInfo);
            if (matcher.find()) {
                String group = matcher.group();
                group = group.substring(group.indexOf("=")+1, group.length()-1); //extract the data
                data.put(option.key(), option.cast(group));
            } else if (defaultConfig != null && defaultConfig.contains(option.key())) { //use defaultConfig
                data.put(option.key(), option.cast(defaultConfig.getString(option.key())));
            } else { //use predefined default
                data.put(option.key(), option.cast(option.value()));
            }
            combiners.put(option.key(), option.getCombiner());
        }
    }
    
    public EffectData(Map<String,Object> data, Map<String,EffectDataCombiner> combiners) {
        this.data = data;
        this.combiners = combiners;
    }
    
    public Object get(String key) {
        return data.get(key);
    }
    
    public boolean contains(String key) {
        return data.containsKey(key);
    }
    
    public Double getDouble(String key) {
        Object o = get(key);
        return o instanceof Number ? ((Number)o).doubleValue() : 0;
    }
    
    public Integer getInt(String key) {
        Object o = get(key);
        return o instanceof Number ? ((Number)o).intValue(): 0;
    }
    
    public Float getFloat(String key) {
        Object o = get(key);
        return o instanceof Number ? ((Number)o).floatValue(): 0;
    }
    
    public Boolean getBoolean(String key) {
        return Boolean.valueOf(getString(key));
    }
    
    public String getString(String key) {
        return String.valueOf(get(key));
    }
    
    public boolean isSimmilar(EffectData otherData) {
        return this.getClass().equals(otherData.getClass()) && otherData.data.keySet().equals(data.keySet());
    }
    
    public void combine(EffectData otherData) {
        for (String key : data.keySet()) {
            data.put(key, combiners.get(key).combine(data.get(key), otherData.get(key)));
        }
    }
    
    public void remove(EffectData otherData) {
        for (String key : data.keySet()) {
            data.put(key, combiners.get(key).remove(data.get(key), otherData.get(key)));
        }
    }
    
    @Override
    public EffectData clone() {
        try {
            Bukkit.getLogger().info("Clone");
            Constructor<? extends EffectData> constructor = this.getClass().getConstructor(Map.class, Map.class);
            return constructor.newInstance(new HashMap<>(data),new HashMap<>(combiners));
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to find clone constructor for EffectDataClass "+this.getClass().getSimpleName()+"! Using default EffectData",ex);
            return new EffectData(data,combiners);
        }
    }
    
}
