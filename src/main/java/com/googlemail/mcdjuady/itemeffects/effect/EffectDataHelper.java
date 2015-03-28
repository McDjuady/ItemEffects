/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.effect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;

/**
 *
 * @author Max
 */
public class EffectDataHelper {

    private final static String basePattern = "!%1$s=(((-|\\+)?\\d+(\\.\\d+)?)|\\w+)(!|\\|)";

    private final EffectDataOption option;
    private Method valueOf;
    private final Pattern pattern;
    private EffectDataCombiner combiner;
    private boolean canEnchant;

    public EffectDataHelper(EffectDataOption option) {
        this.option = option;
        this.canEnchant = option.canEnchant();
        this.pattern = Pattern.compile(String.format(basePattern, option.key().toLowerCase()));
        try {
            this.valueOf = option.dataClass().getMethod("valueOf", String.class);
        } catch (NoSuchMethodException | SecurityException ex) {
            //Ignore
        }
        try {
            combiner = option.combiner().getConstructor().newInstance();
        } catch (NoSuchMethodException | SecurityException ex) {
            Bukkit.getLogger().log(Level.WARNING, "No empty constructor for " + option.combiner().getSimpleName() + "! Using AddCombiner", ex);
            combiner = new EffectDataAddCombiner();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to instantiate " + option.combiner().getSimpleName() + "! Using AddCombiner", ex);
            combiner = new EffectDataAddCombiner();
        }

    }

    public Object cast(String string) {
        if (valueOf != null) {
            try {
                return valueOf.invoke(null, string);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Bukkit.getLogger().log(Level.WARNING, "Failed to acess valueOf method!", ex);
            }
        }
        try {
            return option.dataClass().cast(string);
        } catch (ClassCastException ex) { //just return the string
            return string;
        }
    }

    public Matcher matcher(String string) {
        return pattern.matcher(string.toLowerCase());
    }

    public String key() {
        return option.key();
    }
    
    public String value() {
        return option.value();
    }

    public Class<?> dataClass() {
        return option.dataClass();
    }

    public EffectDataCombiner getCombiner() {
        return combiner;
    }

    /**
     * @return the canEnchant
     */
    public boolean canEnchant() {
        return canEnchant;
    }

}
