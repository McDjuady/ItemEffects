/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.effects;

import com.googlemail.mcdjuady.itemeffects.Effect;
import com.googlemail.mcdjuady.itemeffects.EffectData;
import com.googlemail.mcdjuady.itemeffects.EffectHandler;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

/**
 *
 * @author Max
 */
public class ResistanceEffect extends Effect{
    
    private final DamageCause resistanceType;
    
    public ResistanceEffect(ConfigurationSection effectConfig) {
        super(effectConfig);
        resistanceType = DamageCause.valueOf(effectConfig.getString("DamageType"));
        String chance = effectConfig.getString("ResistAmount");
        setDefaultData(new String[]{chance});
    }
    
    @EffectHandler({EntityDamageByBlockEvent.class,EntityDamageByEntityEvent.class,EntityDamageEvent.class})
    public void onDamage(EffectData data, Player player, EntityDamageEvent e) {
        if (!e.getEntity().equals(player)) {
            return;
        }
        double amount = data.get(0);
        if (resistanceType == e.getCause()) {
            e.setDamage(e.getDamage() - amount);
        }
        if (resistanceType == DamageCause.FIRE && (e.getCause() == DamageCause.FIRE_TICK || e.getCause() == DamageCause.LAVA)) {
            e.setDamage(e.getDamage() - amount);
        }
        if (resistanceType == DamageCause.ENTITY_ATTACK && e.getCause() == DamageCause.PROJECTILE) {
            e.setDamage(e.getDamage() - amount);
        }
    }
    
}
