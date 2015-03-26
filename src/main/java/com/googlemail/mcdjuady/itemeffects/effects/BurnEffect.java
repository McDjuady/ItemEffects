/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.effects;

import com.googlemail.mcdjuady.itemeffects.effect.Effect;
import com.googlemail.mcdjuady.itemeffects.effect.EffectData;
import com.googlemail.mcdjuady.itemeffects.effect.EffectDataMaxCombiner;
import com.googlemail.mcdjuady.itemeffects.effect.EffectDataOption;
import com.googlemail.mcdjuady.itemeffects.effect.EffectHandler;
import com.googlemail.mcdjuady.itemeffects.effect.EffectOptions;
import java.util.Random;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Max
 */
@EffectOptions(global = true, dataOptions = {
    @EffectDataOption(key = "BurnChance", value = "20.0"),
    @EffectDataOption(key = "BurnTime", dataClass = Integer.class, value = "5", combiner = EffectDataMaxCombiner.class)})
public class BurnEffect extends Effect {

    private final Random random = new Random();

    public BurnEffect(ConfigurationSection effectConfig, ItemStack item, String lore) throws InvalidConfigurationException {
        super(effectConfig, item, lore);
    }

    public BurnEffect(ConfigurationSection effectConfig, ItemStack item, String[] args) throws InvalidConfigurationException {
        super(effectConfig, item, args);
    }

    @EffectHandler(EntityDamageByEntityEvent.class)
    public void onEntityDamageByEntity(EffectData data, Player player, EntityDamageByEntityEvent event) {
        Bukkit.getLogger().info("BurnEffect");
        if (!player.equals(event.getDamager())) {
            return;
        }
        Entity damagee = event.getEntity();
        double chance = data.getDouble("BurnChance");
        Bukkit.getLogger().log(Level.INFO, "Try burn (chance {0})", chance);
        if (chance > random.nextInt(100)) {
            damagee.setFireTicks((int) data.getInt("BurnTime") * 20);
        }
    }
}
