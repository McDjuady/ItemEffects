/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.effect;

import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Max
 */
public class DelayedInventoryUpdate extends BukkitRunnable {
    private final PlayerEffects playerEffects;
    private final boolean inHandOnly;

    public DelayedInventoryUpdate(PlayerEffects effects, boolean inHandOnly) {
        this.playerEffects = effects;
        this.inHandOnly = inHandOnly;
    }

    @Override
    public void run() {
        if (inHandOnly) {
            playerEffects.updateItemInHand();
        } else {
            playerEffects.updateInventory();
        }
    }
    
}
