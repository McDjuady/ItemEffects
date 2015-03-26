/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.event;

import com.googlemail.mcdjuady.itemeffects.effect.EffectData;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 *
 * @author Max
 */
public class GlobalUpdateEvent extends GlobalEvent{
    private static final HandlerList handlers = new HandlerList();

    public GlobalUpdateEvent(Player player, EffectData globalData) {
        super(player, globalData);
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
