/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.effects;

import com.googlemail.mcdjuady.itemeffects.ActiveEffects;
import com.googlemail.mcdjuady.itemeffects.Effect;
import com.googlemail.mcdjuady.itemeffects.EffectData;
import com.googlemail.mcdjuady.itemeffects.EffectHandler;
import com.googlemail.mcdjuady.itemeffects.EffectTarget;
import com.googlemail.mcdjuady.itemeffects.ItemEffects;
import com.googlemail.mcdjuady.itemeffects.FilterGroups;
import com.googlemail.mcdjuady.itemeffects.event.ItemEquipEvent;
import com.googlemail.mcdjuady.itemeffects.event.ItemSelectEvent;
import com.googlemail.mcdjuady.itemeffects.event.PlayerItemEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Max
 */
@EffectTarget(FilterGroups.ANY)
public class LevelEffect extends Effect {
    
    private class EffectTask extends BukkitRunnable {
        
        private final Player player;
        private final ActiveEffects effects;
        private final LevelEffect parent;
        
        public EffectTask(LevelEffect effect, Player player) {
            super();
            this.player = player;
            this.effects = ItemEffects.getInstance().getEffectManager().getActiveEffects(player);
            this.parent = effect;
        }
        
        @Override
        public void run() {
            EffectData data = effects.getEffectData(parent);
            if (!player.isOnline() || data == null || data.get(0) < player.getLevel()) {
                parent.remove(player);
                Bukkit.getScheduler().cancelTask(this.getTaskId());
                return;
            }
            Bukkit.getLogger().log(Level.INFO, "Run for {0}", player.getName());
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 39, 50, true, false), true);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 39, 50, true, false), true);
        }
        
    }
    
    private final Map<UUID, Integer> tasks;
    
    private void remove(Player player) {
        tasks.remove(player.getUniqueId());
    }
    
    public LevelEffect(ConfigurationSection effectConfig) {
        super(effectConfig);
        String level = effectConfig.getString("Level");
        setDefaultData(new String[]{level});
        tasks = new HashMap<>();
    }
    
    @EffectHandler({ItemEquipEvent.class,ItemSelectEvent.class})
    public void onEquip(EffectData data, Player player, PlayerItemEvent e) {
        double requiredLevel = data.get(0);
        int playerLevel = player.getLevel();
        if (requiredLevel > playerLevel) {
            if (!tasks.containsKey(player.getUniqueId())) {
                Bukkit.getLogger().info("Start task");
                tasks.put(player.getUniqueId(), new EffectTask(this,player).runTaskTimer(ItemEffects.getInstance(), 0, 10).getTaskId());
            }
        }
    }
    
}
