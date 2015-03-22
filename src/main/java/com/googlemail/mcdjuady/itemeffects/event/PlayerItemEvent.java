/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.event;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Max
 */
public abstract class PlayerItemEvent extends PlayerEvent{
    
    private final ItemStack item;
    
    public PlayerItemEvent(Player player, ItemStack item) {
        super(player);
        this.item = item;
    }
    
    public ItemStack getItem() {
        return item;
    }
}
