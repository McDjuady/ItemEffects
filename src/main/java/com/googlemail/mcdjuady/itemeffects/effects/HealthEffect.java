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
import com.googlemail.mcdjuady.itemeffects.event.ItemActivateEvent;
import com.googlemail.mcdjuady.itemeffects.event.ItemDeactivateEvent;
import com.googlemail.mcdjuady.itemeffects.event.PlayerItemEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Max
 */
@EffectOptions(dataOptions = @EffectDataOption(key = "Health", dataClass = Integer.class, value = "10"))
public class HealthEffect extends Effect{
    
    public HealthEffect(ConfigurationSection effectConfig, ItemStack item, String lore) throws InvalidConfigurationException {
        super(effectConfig, item, lore);
    }

    public HealthEffect(ConfigurationSection effectConfig, ItemStack item, String[] args) throws InvalidConfigurationException {
        super(effectConfig, item, args);
    }
    
    @EffectHandler(ItemActivateEvent.class)
    public void onActivate(EffectData data, Player player, PlayerItemEvent e) {
        player.setMaxHealth(player.getMaxHealth() + data.getInt("Health"));
    }
    
    @EffectHandler(ItemDeactivateEvent.class)
    public void onDeactivate(EffectData data, Player player, PlayerItemEvent e) {
        player.setMaxHealth(player.getMaxHealth() - data.getInt("Health"));
    }
    
}
