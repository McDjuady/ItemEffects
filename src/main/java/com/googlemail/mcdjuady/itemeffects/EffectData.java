/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;

/**
 *
 * @author Max
 */
public class EffectData {

    private static abstract class Combiner {

        private static final Pattern pattern = Pattern.compile("^\\d+(.\\d+)?");

        public abstract Double combine(Double i1, Double i2);

        public static final Double getValue(String info) {
            Matcher matcher = pattern.matcher(info);
            if (matcher.find()) {
                return Double.valueOf(matcher.group());
            }
            return 0D;
        }

    }

    private static class AddCombiner extends Combiner {

        public static final Pattern pattern = Pattern.compile("^([+,-])?\\d+(.\\d+)?$");

        @Override
        public Double combine(Double i1, Double i2) {
            return i1 + i2;
        }
    }

    private static class MinCombiner extends Combiner {

        public static final Pattern pattern = Pattern.compile("^min\\d+(.\\d+)?$");

        @Override
        public Double combine(Double i1, Double i2) {
            return Math.min(i1, i2);
        }

    }

    private static class MaxCombiner extends Combiner {

        public static final Pattern pattern = Pattern.compile("^max\\d+(.\\d+)?$");

        @Override
        public Double combine(Double i1, Double i2) {
            return Math.max(i1, i2);
        }

    }

    private final Effect effect;
    private final List<Double> data;
    private final List<Combiner> combiners;

    private EffectData(Effect effect, List<Double> data, List<Combiner> combiners) {
        this.data = data;
        this.effect = effect;
        this.combiners = combiners;
    }

    public EffectData(Effect effect, String[] infoSplit) {
        data = new ArrayList<>();
        combiners = new ArrayList<>();
        this.effect = effect;
        String[] defaultData = effect.getDefaultData();
        for (int i = 0; i < defaultData.length; i++) {
            String info;
            if (i + 1 < infoSplit.length) {
                info = infoSplit[i + 1].toLowerCase();//shift one to the right since infoSplit[0] is the effectName
            } else {
                info = defaultData[i].toLowerCase();
            }
            if (MaxCombiner.pattern.matcher(info).find()) {
                Bukkit.getLogger().log(Level.INFO, "MaxCombiner for {0}", info);
                combiners.add(new MaxCombiner());
            } else if (MinCombiner.pattern.matcher(info).find()) {
                Bukkit.getLogger().log(Level.INFO, "MinCombiner for {0}", info);
                combiners.add(new MinCombiner());
            } else {
                Bukkit.getLogger().log(Level.INFO, "AddCombiner for {0}", info);
                combiners.add(new AddCombiner());
            }
            data.add(Combiner.getValue(info));
        }
    }

    public EffectData combine(EffectData effectData) {
        if (effectData == null || !effect.equals(effectData.effect)) {
            return null;
        }
        List<Double> newData = new ArrayList<>();
        for (int i = 0; i < data.size() && i < effectData.data.size(); i++) {
            newData.add(combiners.get(i).combine(data.get(i), effectData.data.get(i)));
        }
        return new EffectData(getEffect(), newData, combiners);
    }

    public double get(int i) {
        return data.get(i);
    }

    public int size() {
        return data.size();
    }

    public List<Double> getData() {
        return data;
    }

    /**
     * @return the effect
     */
    public Effect getEffect() {
        return effect;
    }

    @Override
    public String toString() {
        return "EffectData ("+effect.getName()+"): "+data.toString(); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
