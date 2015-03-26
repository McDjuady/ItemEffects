/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.event;

import com.googlemail.mcdjuady.itemeffects.event.PlayerItemEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Max
 */
public class ItemActivateEvent extends PlayerItemEvent{
    
    private static final HandlerList handlers = new HandlerList();

    public ItemActivateEvent(Player player, ItemStack item) {
        super(player, item);
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
}
