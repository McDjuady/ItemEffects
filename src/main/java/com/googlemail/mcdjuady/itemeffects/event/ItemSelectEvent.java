/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Max
 */
public class ItemSelectEvent extends PlayerEvent{
    
    private final static HandlerList handlers = new HandlerList();
    private final ItemStack item;
    
    public ItemSelectEvent(Player player, ItemStack item) {
        super(player);
        this.item = item;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    /**
     * @return the item
     */
    public ItemStack getItem() {
        return item;
    }
    
}
