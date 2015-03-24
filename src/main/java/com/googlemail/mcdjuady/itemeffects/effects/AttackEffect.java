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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 *
 * @author Max
 */
public class AttackEffect extends Effect{

    public AttackEffect(ConfigurationSection effectConfig) {
        super(effectConfig);
        String amount = effectConfig.getString("Amount");
        setDefaultData(new String[]{amount});
    }
    
    @EffectHandler(EntityDamageByEntityEvent.class)
    public void onEntityDamageByEntity(EffectData data, Player player, EntityDamageByEntityEvent event) {
        if (event.getDamager().equals(player)) {
            event.setDamage(event.getDamage() + data.get(0));
        }
    }
    
}
