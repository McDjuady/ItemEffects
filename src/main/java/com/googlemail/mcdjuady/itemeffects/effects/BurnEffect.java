/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.effects;

import com.googlemail.mcdjuady.itemeffects.Effect;
import com.googlemail.mcdjuady.itemeffects.EffectData;
import com.googlemail.mcdjuady.itemeffects.EffectHandler;
import com.googlemail.mcdjuady.itemeffects.EffectTarget;
import com.googlemail.mcdjuady.itemeffects.FilterGroups;
import java.util.Random;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 *
 * @author Max
 */
@EffectTarget(FilterGroups.SWORD)
public class BurnEffect extends Effect {

    private final Random random = new Random();

    public BurnEffect(ConfigurationSection effectConfig) {
        super(effectConfig);
        String chance = effectConfig.getString("BurnChance");
        String duration = effectConfig.getString("BurnDuration");
        setDefaultData(new String[]{chance,duration});
    }

    @EffectHandler(EntityDamageByEntityEvent.class)
    public void onEntityDamageByEntity(EffectData data, Player player, EntityDamageByEntityEvent event) {
        Bukkit.getLogger().info("BurnEffect");
        if (!player.equals(event.getDamager())) {
            return;
        }
        Entity damagee = event.getEntity();
        double chance = data.get(0);
        Bukkit.getLogger().log(Level.INFO, "Try burn (chance {0})", chance);
        if (chance > random.nextInt(100)) {
            damagee.setFireTicks((int)Math.round(data.get(1)) * 20);
        }
    }
}
