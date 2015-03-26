/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.event;

import com.googlemail.mcdjuady.itemeffects.effect.EffectData;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;

/**
 *
 * @author Max
 */
public abstract class GlobalEvent extends PlayerEvent{

    private final EffectData globalData;
    
    public GlobalEvent(Player who, EffectData globalData) {
        super(who);
        this.globalData = globalData; 
    }
    
    public EffectData getGlobalData() {
        return globalData;
    }
    
}
