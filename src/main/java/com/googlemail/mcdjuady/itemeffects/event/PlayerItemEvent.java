/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.event;

import com.googlemail.mcdjuady.itemeffects.EffectManager;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Max
 */
public abstract class PlayerItemEvent extends PlayerEvent{
    
    private final ItemStack item;
    private final int slot;
    
    public PlayerItemEvent(Player player, int slot) {
        super(player);
        //we have to add 4 since the 4 armor slots get left out in .getSize()
        if (slot > (player.getInventory().getSize() + 4) || (slot < 0 && slot != EffectManager.GLOBALSLOT && slot != EffectManager.INHANDSLOT)) {
            throw new IllegalArgumentException("Invalid slot! "+slot);
        }
        this.slot = slot;
        if (slot == EffectManager.INHANDSLOT) {
            slot = player.getInventory().getHeldItemSlot();
        }
        if (slot == EffectManager.GLOBALSLOT) {
            this.item = null;
        } else {
            this.item = player.getInventory().getItem(slot);
        }
    }
    
    public ItemStack getItem() {
        return item;
    }
    
    public int getSlot() {
        return slot;
    }
}
