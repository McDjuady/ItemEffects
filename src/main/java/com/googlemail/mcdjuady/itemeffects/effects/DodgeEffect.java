/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.effects;

import com.googlemail.mcdjuady.itemeffects.ActiveEffects;
import com.googlemail.mcdjuady.itemeffects.Effect;
import com.googlemail.mcdjuady.itemeffects.EffectData;
import com.googlemail.mcdjuady.itemeffects.EffectHandler;
import com.googlemail.mcdjuady.itemeffects.EffectTarget;
import com.googlemail.mcdjuady.itemeffects.FilterGroups;
import java.util.Random;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 *
 * @author Max
 */

@EffectTarget(FilterGroups.ARMOR)
public class DodgeEffect extends Effect{
    
    private final Random random = new Random();
    
    public DodgeEffect(ConfigurationSection effectConfig) {
        super(effectConfig);
        String chance = effectConfig.getString("DodgeChance");
        setDefaultData(new String[]{chance});
    }
    
    @EffectHandler(EntityDamageByEntityEvent.class)
    public void onEntityDamage(EffectData data, Player player, EntityDamageByEntityEvent e) {
        if (e.getEntity().equals(player)) {
            int chance = data.get(0);
            if (chance > random.nextInt(100)) {
                e.setCancelled(true);
            }
        }
    }
    
}
