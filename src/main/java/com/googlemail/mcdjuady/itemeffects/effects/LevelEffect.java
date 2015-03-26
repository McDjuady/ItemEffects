/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.effects;

import com.googlemail.mcdjuady.itemeffects.effect.Effect;
import com.googlemail.mcdjuady.itemeffects.effect.EffectData;
import com.googlemail.mcdjuady.itemeffects.effect.EffectDataOption;
import com.googlemail.mcdjuady.itemeffects.effect.EffectHandler;
import com.googlemail.mcdjuady.itemeffects.effect.EffectOptions;
import com.googlemail.mcdjuady.itemeffects.ItemEffects;
import com.googlemail.mcdjuady.itemeffects.effect.EffectDataMaxCombiner;
import com.googlemail.mcdjuady.itemeffects.event.GlobalActivateEvent;
import com.googlemail.mcdjuady.itemeffects.event.GlobalDeactivateEvent;
import com.googlemail.mcdjuady.itemeffects.event.GlobalUpdateEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Max
 */
@EffectOptions(global = true, recalculateGlobal = true, dataOptions = {
    @EffectDataOption(key = "Level", dataClass = Integer.class, value = "5", combiner = EffectDataMaxCombiner.class)
})
public class LevelEffect extends Effect {

    private class EffectTask extends BukkitRunnable {

        private final Player player;
        private EffectData globalData;

        public EffectTask(EffectData globalData, Player player) {
            super();
            this.player = player;
            this.globalData = globalData;
        }
        
        public void updateData(EffectData newData) {
            globalData = newData;
        }
        
        @Override
        public void run() {
            if (!player.isOnline() || globalData.getInt("Level") <= player.getLevel()) {
                Bukkit.getScheduler().cancelTask(this.getTaskId());
                return;
            }
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 39, 50, true, false), true);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 39, 50, true, false), true);
        }

    }

    private EffectTask task;

    public LevelEffect(ConfigurationSection effectConfig, ItemStack item, String lore) throws InvalidConfigurationException {
        super(effectConfig, item, lore);
    }

    public LevelEffect(ConfigurationSection effectConfig, ItemStack item, String[] args) throws InvalidConfigurationException {
        super(effectConfig, item, args);
    }

    @EffectHandler(GlobalActivateEvent.class)
    public void onActivate(EffectData data, Player player, GlobalActivateEvent e) {
        int requiredLevel = e.getGlobalData().getInt("Level");
        int playerLevel = player.getLevel();
        if (requiredLevel > playerLevel) {
            task = new EffectTask(e.getGlobalData(), player);
            task.runTaskTimer(ItemEffects.getInstance(), 0, 10);
        }
    }

    @EffectHandler(GlobalUpdateEvent.class)
    public void onUpdate(EffectData data, Player player, GlobalUpdateEvent e) {
        //if we have a task update the data to the recombined global data
        if (task != null) {
            task.updateData(e.getGlobalData());
        }
        //only update if the task is not running
        if (task == null || (!Bukkit.getScheduler().isQueued(task.getTaskId()) && !Bukkit.getScheduler().isCurrentlyRunning(task.getTaskId()))) {
            int requiredLevel = e.getGlobalData().getInt("Level");
            int playerLevel = player.getLevel();
            if (requiredLevel > playerLevel) {
                task = new EffectTask(e.getGlobalData(), player);
                task.runTaskTimer(ItemEffects.getInstance(), 0, 10);
            }
        }
        
    }

    @EffectHandler(GlobalDeactivateEvent.class)
    public void onDeactivate(EffectData data, Player player, GlobalDeactivateEvent e) {
        if (task != null) {
            Bukkit.getScheduler().cancelTask(task.getTaskId());
        }
    }

}
