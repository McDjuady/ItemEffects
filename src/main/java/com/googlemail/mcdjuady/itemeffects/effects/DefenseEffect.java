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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Max
 */
@EffectOptions(global = true, dataOptions = {
    @EffectDataOption(key = "Defense", dataClass = Integer.class, value = "5")
})
public class DefenseEffect extends Effect{

    public DefenseEffect(ConfigurationSection effectConfig, ItemStack item, String lore) throws InvalidConfigurationException {
        super(effectConfig, item, lore);
    }

    public DefenseEffect(ConfigurationSection effectConfig, ItemStack item, String[] args) throws InvalidConfigurationException {
        super(effectConfig, item, args);
    }
    
    @EffectHandler(EntityDamageByEntityEvent.class)
    public void onDamage(EffectData data, Player player, EntityDamageByEntityEvent e) {
        if (e.getEntity().equals(player)) {
            double damage = (e.getDamage() - data.getInt("Defense")) / data.getInt("Defense");
            e.setDamage(damage);
            
        }
    }
    
}
