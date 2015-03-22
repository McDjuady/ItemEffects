/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects;

import com.googlemail.mcdjuady.itemeffects.FilterGroups;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Max
 */
public abstract class Effect {

    private String[] defaultData;
    private final String name;
    private final String humanName;
    private final ItemFilter filter;

    public Effect(ConfigurationSection effectConfig) {
        name = effectConfig.getName();
        humanName = effectConfig.getString("Name");
        //maybe the effect want's to handle the defaultData itself
        if (effectConfig.contains("DefaultData")) {
            List<String> sList = effectConfig.getStringList("DefaultData");
            defaultData = new String[sList.size()];
            defaultData = sList.toArray(defaultData);
        }
        if (effectConfig.contains("Filter")) {
            List<String> filterList = effectConfig.getStringList("Filter");
            if (filterList.isEmpty()) {
                filterList.add(effectConfig.getString("Filter"));
            }
            filter = new ItemFilter(filterList);
        } else {
            EffectTarget target = this.getClass().getAnnotation(EffectTarget.class);
            if (target == null) {
                filter = new ItemFilter(FilterGroups.ANY);
            } else {
                filter = new ItemFilter(target.value());
            }
        }
    }

    public String[] getDefaultData() {
        return defaultData;
    }

    protected void setDefaultData(String[] defaultData) {
        this.defaultData = defaultData;
    }

    public String getName() {
        return name;
    }

    public String getHumanName() {
        return humanName;
    }

    public boolean canApply(ItemStack item) {
        return filter.isValid(item);
    }

    @Override
    public String toString() {
        return "Effect(" + name + ")"; //To change body of generated methods, choose Tools | Templates.
    }

}
