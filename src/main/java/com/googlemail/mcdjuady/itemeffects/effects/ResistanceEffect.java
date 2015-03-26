/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.effects;

import com.googlemail.mcdjuady.itemeffects.effect.Effect;
import com.googlemail.mcdjuady.itemeffects.effect.EffectData;
import com.googlemail.mcdjuady.itemeffects.effect.EffectDataOption;
import com.googlemail.mcdjuady.itemeffects.effect.EffectHandler;
import com.googlemail.mcdjuady.itemeffects.effect.EffectOptions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Max
 */
@EffectOptions(dataOptions = {
    @EffectDataOption(key = "ResistType", dataClass = String.class, value = "ENTITY_ATTACK"),
    @EffectDataOption(key = "ResistAmount", value = "5.0"),
})
public class ResistanceEffect extends Effect{
    
    private final DamageCause resistanceType;
    
    public ResistanceEffect(ConfigurationSection effectConfig, ItemStack item, String lore) throws InvalidConfigurationException {
        super(effectConfig, item, lore);
        resistanceType = DamageCause.valueOf(getEffectData().getString("ResistType"));
    }

    public ResistanceEffect(ConfigurationSection effectConfig, ItemStack item, String[] args) throws InvalidConfigurationException {
        super(effectConfig, item, args);
        resistanceType = DamageCause.valueOf(getEffectData().getString("ResistType"));
    }
    
    @EffectHandler({EntityDamageByBlockEvent.class,EntityDamageByEntityEvent.class,EntityDamageEvent.class})
    public void onDamage(EffectData data, Player player, EntityDamageEvent e) {
        if (!e.getEntity().equals(player)) {
            return;
        }
        double amount = data.getDouble("ResistAmount");
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
