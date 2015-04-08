/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects;

import com.googlemail.mcdjuady.itemeffects.effect.EffectItemListener;
import com.googlemail.mcdjuady.itemeffects.filter.FilterGroups;
import com.googlemail.mcdjuady.itemeffects.filter.ItemFilter;
import com.googlemail.mcdjuady.itemeffects.commands.CommandItemEffects;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Max
 */
public class ItemEffects extends JavaPlugin {

    private static ItemEffects instance;

    public static ItemEffects getInstance() {
        if (instance == null) {
            instance = (ItemEffects) Bukkit.getPluginManager().getPlugin("ItemEffects");
        }
        return instance;
    }

    private EffectManager effectManager;
    private Map<Integer, ItemFilter> effectSlots;
    private ItemFilter inHandFilter;

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
        Bukkit.getPluginManager().registerEvents(new EffectItemListener(effectManager), this);
        Bukkit.getPluginManager().registerEvents(new EffectEventListener(effectManager), this);
        this.getCommand("ItemEffects").setExecutor(new CommandItemEffects());
        for (Player player : Bukkit.getOnlinePlayers()) {
            effectManager.onPlayerJoin(player);
        }
    }

    @Override
    public void onDisable() {
        effectManager.onDisable();
    }
    
    @Override
    public void reloadConfig() {
        super.reloadConfig();
        effectSlots = new HashMap<>();
        ConfigurationSection slotsSection = getConfig().getConfigurationSection("EffectSlots");
        createFilters(slotsSection);
    }

    private void createFilters(ConfigurationSection slotsSection) {
        for (String key : slotsSection.getKeys(false)) {
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
                    }
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

    public Map<Integer, ItemFilter> getEffectSlots() {
        return effectSlots;
    }

    public ItemFilter getInHandFilter() {
        return inHandFilter;
    }

}
