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
import com.googlemail.mcdjuady.itemeffects.Util;
import com.googlemail.mcdjuady.itemeffects.event.ItemDeselectEvent;
import com.googlemail.mcdjuady.itemeffects.event.ItemEquipEvent;
import com.googlemail.mcdjuady.itemeffects.event.ItemSelectEvent;
import com.googlemail.mcdjuady.itemeffects.event.ItemUnequipEvent;
import com.googlemail.mcdjuady.itemeffects.event.PlayerItemEvent;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 *
 * @author Max
 */
@EffectTarget(FilterGroups.ARMOR)
public class HealthEffect extends Effect{
    
    private static final int DEFAULT_HEALTH = 20;
    
    public HealthEffect(ConfigurationSection effectConfig) {
        super(effectConfig);
        String health = effectConfig.getString("Health");
        setDefaultData(new String[]{health});
    }
    
    @EffectHandler({ItemEquipEvent.class, ItemSelectEvent.class})
    public void onEquip(EffectData data, Player player, PlayerItemEvent e) {
        player.setMaxHealth(DEFAULT_HEALTH + data.get(0));
    }
    
    @EffectHandler({ItemUnequipEvent.class, ItemDeselectEvent.class})
    public void onUnequip(EffectData data, Player player, PlayerItemEvent e) {
        List<EffectData> effects = Util.getItemEffects(e.getItem());
        for (EffectData effectData : effects) {
            if (effectData.getEffect() instanceof HealthEffect) {
                player.setMaxHealth(player.getMaxHealth() - effectData.get(0));
                return;
            }
        }
    }
    
}
