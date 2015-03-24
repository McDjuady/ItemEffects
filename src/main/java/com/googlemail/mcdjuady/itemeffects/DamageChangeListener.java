/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects;

import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 *
 * @author Max
 */
public class DamageChangeListener implements Listener{
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntityType() != EntityType.PLAYER) {
            return;
        }
        Player player = (Player)e.getEntity();
        switch (e.getCause()) {
            case POISON:
            case WITHER:
                double tickDamage = player.getMaxHealth() * 0.04 + .5; //4%
                e.setDamage(tickDamage);
                //make shure poison is non lethal
                if (e.getCause() == EntityDamageEvent.DamageCause.POISON && e.getFinalDamage() > player.getHealth()) {
                    e.setDamage(Math.min(tickDamage,player.getHealth()+1D));
                }
                Bukkit.getLogger().log(Level.INFO, "Damage to {0}", e.getFinalDamage());
                break;
            case FIRE_TICK:
                double fireDamage = player.getMaxHealth() * 0.03 + .5; //3%
                
                e.setDamage(fireDamage);
                Bukkit.getLogger().log(Level.INFO, "Fire damage to {0}", e.getFinalDamage());
                break;
        }
    }
    
}
