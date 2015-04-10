/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.effect;

import com.googlemail.mcdjuady.itemeffects.EffectManager;
import com.googlemail.mcdjuady.itemeffects.ItemEffects;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Max
 */
public class InventoryUpdateTask extends BukkitRunnable{

    private HashMap<Player,Boolean> playersToUpdate;
    private final EffectManager manager;
    
    public InventoryUpdateTask() {
        playersToUpdate = new HashMap<>();
        manager = ItemEffects.getInstance().getEffectManager();
    }
    
    public void scheduleUpdate(Player player) {
        scheduleUpdate(player, false);
    }
    
    public void scheduleUpdate(Player player, boolean inHandOnly) {
        if (playersToUpdate.containsKey(player)) {
            boolean current = playersToUpdate.get(player);
            if (!current) {
                return;
            }
            playersToUpdate.put(player, current && inHandOnly);
        } else {
            playersToUpdate.put(player, inHandOnly);
        }
    }
    
    @Override
    public void run() {
        if (playersToUpdate.isEmpty()) {
            return;
        }
        for (Entry<Player,Boolean> entry : playersToUpdate.entrySet()) {
            if (!entry.getKey().isOnline()) {
                continue;
            }
            PlayerEffects effects = manager.getPlayerEffects(entry.getKey());
            if (entry.getValue()) {
                effects.updateItemInHand();
            } else {
                effects.updateInventory();
            }
        }
        playersToUpdate = new HashMap<>();
    }
    
}
