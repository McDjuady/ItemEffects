/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.effect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

/**
 *
 * @author Max
 */
public class EffectData implements Cloneable {

    private final static char DataSeperator = '!';

    private final Set<String> forcedValues;
    private final Map<String, Object> data;
    private final Map<String, EffectDataCombiner> combiners;
    private final String effectName;

    public EffectData(EffectDataHelper[] options, String effectInfo, ConfigurationSection defaultConfig, String effectName) {
        this.data = new HashMap<>();
        this.combiners = new HashMap<>();
        this.forcedValues = new HashSet<>();
        this.effectName = effectName;
        for (EffectDataHelper option : options) {
            Matcher matcher = option.matcher(effectInfo != null ? effectInfo : "");
            if (!option.canEnchant()) {
                forcedValues.add(option.key());
            }
            if (option.canEnchant() && matcher.find()) {
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

    public EffectData(Map<String, Object> data, Map<String, EffectDataCombiner> combiners, Set<String> forcedValues, String effectName) {
        this.data = data;
        this.combiners = combiners;
        this.forcedValues = forcedValues;
        this.effectName = effectName;
    }

    //for set to reflect on the item, Effect.setData() should be called
    //this method is inteded to be used with global effects created by plugins
    public void set(String key, Object object) {
        if (!forcedValues.contains(key.toLowerCase())) {
            data.put(key.toLowerCase(), object);
        }
    }

    public String getEffectName() {
        return effectName;
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
            Constructor<? extends EffectData> constructor = this.getClass().getConstructor(Map.class, Map.class, Set.class, String.class);
            return constructor.newInstance(new HashMap<>(data), new HashMap<>(combiners), new HashSet<>(forcedValues), effectName);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            try {
                Constructor<? extends EffectData> constructor = this.getClass().getDeclaredConstructor(Map.class, Map.class, Set.class, String.class);
                return constructor.newInstance(new HashMap<>(data), new HashMap<>(combiners), new HashSet<>(forcedValues), effectName);
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex1) {
                Bukkit.getLogger().log(Level.WARNING, "Failed to find clone constructor for EffectDataClass " + this.getClass().getSimpleName() + "! Using default EffectData", ex);
                return new EffectData(data, combiners, forcedValues, effectName);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder dataString = new StringBuilder();
        for (String key : data.keySet()) {
            if (!forcedValues.contains(key)) {
                dataString.append(DataSeperator).append(key).append('=').append(getString(key));
            }
        }
        return dataString.toString();
    }

    @Override
    public int hashCode() {
        int hashCode = 5;
        hashCode = hashCode * 37 + combiners.hashCode();
        hashCode = hashCode * 37 + data.hashCode();
        hashCode = hashCode * 37 + forcedValues.hashCode();
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EffectData other = (EffectData) obj;
        if (!Objects.equals(this.forcedValues, other.forcedValues)) {
            return false;
        }
        if (!Objects.equals(this.data, other.data)) {
            return false;
        }
        return Objects.equals(this.combiners, other.combiners);
    }

}
