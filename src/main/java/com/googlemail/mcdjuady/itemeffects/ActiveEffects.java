/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Max
 */
public class ActiveEffects {

    private final Player player;
    private Map<Effect, EffectData> effects;
    private final Map<Integer, List<EffectData>> slotData;
    private List<EffectData> inHand;
    
    public ActiveEffects(Player player) {
        this.player = player;
        slotData = new HashMap<>();
        Map<Integer,ItemFilter> slots = ItemEffects.getInstance().getEffectSlots();
        for (int i : slots.keySet()) {
            ItemStack item = player.getInventory().getItem(i);
            ItemFilter filter = slots.get(i);
            slotData.put(i, filter.isValid(item) ? Util.getItemEffects(item) : null);
        }
        updateItemInHand(ItemEffects.getInstance().getInHandFilter().isValid(player.getItemInHand()) ? player.getItemInHand() : null);
    }
    
    public final void updateItemInHand(ItemStack newItem) {
        inHand = Util.getItemEffects(newItem);
        updateEffects();
    }

    public void updateSlot(int i, ItemStack newItem) {
        if (slotData.containsKey(i)) {
            slotData.put(i, Util.getItemEffects(newItem));
            updateEffects();
        }
    }

    private void updateEffects() {
        effects = new HashMap<>();
        for (List<EffectData> list : slotData.values()) {
            if (list == null) {
                continue;
            }
            for (EffectData data : list) {
                EffectData other = effects.get(data.getEffect());
                if (other == null) {
                    other = data;
                } else {
                    other = data.combine(other);
                }
                effects.put(other.getEffect(), other);
            }
        }
        //Add item in hand
        if (inHand != null && !inHand.isEmpty()) {
            for (EffectData data : inHand) {
                EffectData other = effects.get(data.getEffect());
                if (other == null) {
                    other = data;
                } else {
                    other = data.combine(other);
                }
                effects.put(other.getEffect(), other);
            }
        }
        Bukkit.getLogger().log(Level.INFO, "ActiveEffects: {0}", effects.toString());
    }
    
    public Set<Effect> getActiveEffects() {
        return effects.keySet();
    }
    
    public Set<Map.Entry<Effect,EffectData>> getEntrySet() {
        return effects.entrySet();
    }
    
    public EffectData getEffectData(Effect e) {
        return effects.get(e);
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isEmpty() {
        return effects.isEmpty();
    }
}
