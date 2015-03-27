/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects;

import com.googlemail.mcdjuady.itemeffects.effect.PlayerEffects;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 *
 * @author Max
 */
public class EffectEventListener implements Listener{
    private final EffectManager manager;
    
    public EffectEventListener(EffectManager manager) {
        this.manager = manager;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity().getType() == EntityType.PLAYER) {
            PlayerEffects effects = manager.getPlayerEffects((Player)e.getEntity());
            manager.fireEvent(effects, e);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
        if (e.getEntity().getType() == EntityType.PLAYER) {
            PlayerEffects effects = manager.getPlayerEffects((Player)e.getEntity());
            manager.fireEvent(effects, e);
        }
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onEntityAttack(EntityDamageByEntityEvent e) {
        if (e.getDamager().getType() == EntityType.PLAYER) {
            PlayerEffects effects = manager.getPlayerEffects((Player)e.getDamager());
            manager.fireEvent(effects, e);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getEntity().getType() == EntityType.PLAYER) {
            PlayerEffects effects = manager.getPlayerEffects((Player)e.getEntity());
            manager.fireEvent(effects, e);
        }
    }
}
