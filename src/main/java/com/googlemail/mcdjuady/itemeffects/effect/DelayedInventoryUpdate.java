/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.effect;

import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Max
 */
class DelayedInventoryUpdate extends BukkitRunnable {
    private final PlayerEffects playerEffects;
    private final boolean inHandOnly;

    public DelayedInventoryUpdate(PlayerEffects effects, boolean inHandOnly) {
        this.playerEffects = effects;
        this.inHandOnly = inHandOnly;
    }

    @Override
    public void run() {
        Bukkit.getLogger().log(Level.INFO, "Delayed update {0}", inHandOnly);
        if (inHandOnly) {
            playerEffects.updateItemInHand();
        } else {
            playerEffects.updateInventory();
        }
    }
    
}
