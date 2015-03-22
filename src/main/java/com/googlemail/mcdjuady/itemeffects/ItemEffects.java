/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects;

import com.googlemail.mcdjuady.itemeffects.commands.CommandEnchant;
import com.googlemail.mcdjuady.itemeffects.effects.BurnEffect;
import com.googlemail.mcdjuady.itemeffects.effects.DodgeEffect;
import com.googlemail.mcdjuady.itemeffects.effects.HealthEffect;
import com.googlemail.mcdjuady.itemeffects.effects.LevelEffect;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Max
 */
public class ItemEffects extends JavaPlugin{
    
    private static ItemEffects instance;
    
    public static ItemEffects getInstance() {
        if (instance == null) {
            instance = (ItemEffects)Bukkit.getPluginManager().getPlugin("ItemEffects");
        }
        return instance;
    }
    
    private EffectManager effectManager;
    private Map<Integer,ItemFilter> effectSlots;
    private ItemFilter inHandFilter;
    //TODO slotFilter
    
    @Override
    public void onEnable() {
        if (!this.getDataFolder().exists()) {
            this.saveDefaultConfig();
        }
        updateConfig();
        effectSlots = new HashMap<>();
        ConfigurationSection slotsSection = getConfig().getConfigurationSection("EffectSlots");
        createFilters(slotsSection);
        effectManager = new EffectManager();
        Effect burn = new BurnEffect(getConfig().getConfigurationSection("BurnEffect"));
        Effect level = new LevelEffect(getConfig().getConfigurationSection("LevelEffect"));
        Effect dodge = new DodgeEffect(getConfig().getConfigurationSection("DodgeEffect"));
        Effect health = new HealthEffect(getConfig().getConfigurationSection("HealthEffect"));
        effectManager.registerEffect(burn);
        effectManager.registerEffect(level);
        effectManager.registerEffect(dodge);
        effectManager.registerEffect(health);
        Bukkit.getPluginManager().registerEvents(new EffectListener(effectManager), this);
        this.getCommand("iEnchant").setExecutor(new CommandEnchant());
    }
    
    private void createFilters(ConfigurationSection slotsSection) {
        for (String key : slotsSection.getKeys(false)) {
            Bukkit.getLogger().log(Level.INFO, "Try {0}", key);
            if (key.equalsIgnoreCase("InHand")) {
                ConfigurationSection section = slotsSection.getConfigurationSection(key);
                if (section != null && section.contains("Filter")) {
                    List<String> filters = section.getStringList("Filter");
                    if (filters == null) {
                        filters = new ArrayList<>();
                        filters.add(section.getString("Filter"));
                    }
                    inHandFilter = new ItemFilter(filters);
                } else {
                    //Default filter
                    inHandFilter = new ItemFilter(FilterGroups.SWORD, FilterGroups.TOOL, FilterGroups.BOW);
                }
            }
            if (key.matches("^\\d+$")) {
                ConfigurationSection section = slotsSection.getConfigurationSection(key);
                if (section != null && section.contains("Filter")) {
                    List<String> filters = section.getStringList("Filter");
                    if (filters == null || filters.isEmpty()) {
                        filters = new ArrayList<>();
                        filters.add(section.getString("Filter"));
                        Bukkit.getLogger().log(Level.INFO, "Filter: {0}", section.getString("Filter"));
                    }
                    Bukkit.getLogger().log(Level.INFO, "Filters: {0}", filters.toString());
                    Bukkit.getLogger().log(Level.INFO, "SectionKeys: {0}", section.getKeys(true));
                    effectSlots.put(Integer.valueOf(key), new ItemFilter(filters));
                } else {
                    //Default filter
                    int slot = Integer.valueOf(key);
                    switch (slot) {
                        case 39:
                            effectSlots.put(slot, new ItemFilter(FilterGroups.HELMET));
                            break;
                        case 38:
                            effectSlots.put(slot, new ItemFilter(FilterGroups.CHEST));
                            break;
                        case 37:
                            effectSlots.put(slot, new ItemFilter(FilterGroups.LEGS));
                            break;
                        case 36:
                            effectSlots.put(slot, new ItemFilter(FilterGroups.BOOTS));
                            break;
                        default:
                            effectSlots.put(slot, new ItemFilter(FilterGroups.ANY));
                    }
                }
            }
        }
        getLogger().info("Effect Slots:");
        for (int i : effectSlots.keySet()) {
            getLogger().log(Level.INFO, "{0}: {1}", new Object[]{i,effectSlots.get(i).toString()});
        }
        getLogger().log(Level.INFO, "InHand: {0}",inHandFilter.toString());
    }
    
    private void updateConfig() {
        FileConfiguration config = this.getConfig();
        Configuration defaultConfig = config.getDefaults();
        if (config.getKeys(true).equals(defaultConfig.getKeys(true))) {
            return;
        }
        for (String key : defaultConfig.getKeys(true)) {
            if (config.get(key, null) == null) {
                config.set(key, defaultConfig.get(key));
            }
        }
        this.saveConfig();
    }
    
    public EffectManager getEffectManager() {
        return effectManager;
    }
    

    
    public Map<Integer,ItemFilter> getEffectSlots() {
        return effectSlots;
    }
    
    public ItemFilter getInHandFilter() {
        return inHandFilter;
    }
    
}
