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
@EffectOptions(dataOptions = {
    @EffectDataOption(key = "Amount", dataClass = Integer.class, value = "5")})
public class AttackEffect extends Effect {

    public AttackEffect(ConfigurationSection effectConfig, ItemStack item, String lore) throws InvalidConfigurationException {
        super(effectConfig, item, lore);
    }

    public AttackEffect(ConfigurationSection effectConfig, ItemStack item, String[] args) throws InvalidConfigurationException {
        super(effectConfig, item, args);
    }

    @EffectHandler(EntityDamageByEntityEvent.class)
    public void onEntityDamageByEntity(EffectData data, Player player, EntityDamageByEntityEvent event) {
        if (event.getDamager().equals(player)) {
            event.setDamage(event.getDamage() + data.getInt("Amount"));
        }
    }

}
