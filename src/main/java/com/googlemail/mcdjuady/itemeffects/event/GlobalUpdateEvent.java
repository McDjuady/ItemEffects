/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.event;

import com.googlemail.mcdjuady.itemeffects.effect.EffectData;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Max
 */
public class GlobalUpdateEvent extends GlobalEvent{
    private static final HandlerList handlers = new HandlerList();

    public enum UpdateAction {
        ADD,
        REMOVE,
        UPDATE,
    }
    
    private final Object updateData;
    private final UpdateAction action;
    
    public GlobalUpdateEvent(Player player, EffectData globalData, Object updateData, UpdateAction action) {
        super(player, globalData);
        this.updateData = updateData;
        this.action = action;
    }
    
    public <T> T getUpdateData() {
        return (T)updateData;
    }
    
    public UpdateAction getAction() {
        return action;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
