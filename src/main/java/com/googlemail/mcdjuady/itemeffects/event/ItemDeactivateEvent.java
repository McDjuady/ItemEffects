/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Max
 */
public class ItemDeactivateEvent extends PlayerItemEvent{
    
    private static final HandlerList handlers = new HandlerList();

    public ItemDeactivateEvent(Player player, int slot) {
        super(player, slot);
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
}
