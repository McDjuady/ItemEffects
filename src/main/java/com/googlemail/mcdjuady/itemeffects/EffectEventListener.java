/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects;

import com.googlemail.mcdjuady.itemeffects.effect.Effect;
import com.googlemail.mcdjuady.itemeffects.effect.PlayerEffects;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;

/**
 *
 * @author Max
 */
public class EffectEventListener implements Listener {

    private final EffectManager manager;

    public EffectEventListener(EffectManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent e) {
        //only fire for events that aren't handled by EntityDamageByEntity or EntityDamageByBlock
        if (e.getEntity().getType() == EntityType.PLAYER && !(e instanceof EntityDamageByEntityEvent) && !(e instanceof EntityDamageByBlockEvent)) {
            PlayerEffects effects = manager.getPlayerEffects((Player) e.getEntity());
            manager.fireEvent(effects, e);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
        if (e.getEntity().getType() == EntityType.PLAYER) {
            PlayerEffects effects = manager.getPlayerEffects((Player) e.getEntity());
            manager.fireEvent(effects, e);
        }
    }

    //EntityDamageByEntity events are fired sperately for every priority
    //This is to ensure that defensive as well as offensive effects can interact in a good way
    //Within each priority the Attacker will be notified first and then the defender, so the total flow looks like this
    //Attacker.LOWEST,Defender.LOWEST,ATTACKER.Low,Defender.LOW etc
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamageByEntityLowest(EntityDamageByEntityEvent e) {
        Entity attacker = Effect.getAttacker(e);
        if (attacker.getType() == EntityType.PLAYER) {
            PlayerEffects effects = manager.getPlayerEffects((Player) attacker);
            manager.fireEvent(effects, e, EventPriority.LOWEST);
        }
        if (e.getEntity().getType() == EntityType.PLAYER) {
            PlayerEffects effects = manager.getPlayerEffects((Player) e.getEntity());
            manager.fireEvent(effects, e, EventPriority.LOWEST);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamageByEntityLow(EntityDamageByEntityEvent e) {
        Entity attacker = Effect.getAttacker(e);
        if (attacker.getType() == EntityType.PLAYER) {
            PlayerEffects effects = manager.getPlayerEffects((Player) attacker);
            manager.fireEvent(effects, e, EventPriority.LOW);
        }
        if (e.getEntity().getType() == EntityType.PLAYER) {
            PlayerEffects effects = manager.getPlayerEffects((Player) e.getEntity());
            manager.fireEvent(effects, e, EventPriority.LOW);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByEntityNormal(EntityDamageByEntityEvent e) {
        Entity attacker = Effect.getAttacker(e);
        if (attacker.getType() == EntityType.PLAYER) {
            PlayerEffects effects = manager.getPlayerEffects((Player) attacker);
            manager.fireEvent(effects, e, EventPriority.NORMAL);
        }
        if (e.getEntity().getType() == EntityType.PLAYER) {
            PlayerEffects effects = manager.getPlayerEffects((Player) e.getEntity());
            manager.fireEvent(effects, e, EventPriority.NORMAL);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntityHigh(EntityDamageByEntityEvent e) {
        Entity attacker = Effect.getAttacker(e);
        if (attacker.getType() == EntityType.PLAYER) {
            PlayerEffects effects = manager.getPlayerEffects((Player) attacker);
            manager.fireEvent(effects, e, EventPriority.HIGH);
        }
        if (e.getEntity().getType() == EntityType.PLAYER) {
            PlayerEffects effects = manager.getPlayerEffects((Player) e.getEntity());
            manager.fireEvent(effects, e, EventPriority.HIGH);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntityHighest(EntityDamageByEntityEvent e) {
        Entity attacker = Effect.getAttacker(e);
        if (attacker.getType() == EntityType.PLAYER) {
            PlayerEffects effects = manager.getPlayerEffects((Player) attacker);
            manager.fireEvent(effects, e, EventPriority.HIGHEST);
        }
        if (e.getEntity().getType() == EntityType.PLAYER) {
            PlayerEffects effects = manager.getPlayerEffects((Player) e.getEntity());
            manager.fireEvent(effects, e, EventPriority.HIGHEST);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamageByEntityMonitor(EntityDamageByEntityEvent e) {
        Entity attacker = Effect.getAttacker(e);
        if (attacker.getType() == EntityType.PLAYER) {
            PlayerEffects effects = manager.getPlayerEffects((Player) attacker);
            manager.fireEvent(effects, e, EventPriority.MONITOR);
        }
        if (e.getEntity().getType() == EntityType.PLAYER) {
            PlayerEffects effects = manager.getPlayerEffects((Player) e.getEntity());
            manager.fireEvent(effects, e, EventPriority.MONITOR);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerXP(PlayerExpChangeEvent e) {
        PlayerEffects effects = manager.getPlayerEffects(e.getPlayer());
        manager.fireEvent(effects, e);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemDamage(PlayerItemDamageEvent e) {
        PlayerEffects effects = manager.getPlayerEffects(e.getPlayer());
        manager.fireEvent(effects, e);
    }
}
