/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.effect;

import com.googlemail.mcdjuady.itemeffects.EffectManager;
import com.googlemail.mcdjuady.itemeffects.ItemEffects;
import com.googlemail.mcdjuady.itemeffects.filter.ItemFilter;
import com.googlemail.mcdjuady.itemeffects.Util;
import com.googlemail.mcdjuady.itemeffects.event.ItemDeactivateEvent;
import com.googlemail.mcdjuady.itemeffects.event.ItemActivateEvent;
import com.googlemail.mcdjuady.itemeffects.event.GlobalActivateEvent;
import com.googlemail.mcdjuady.itemeffects.event.GlobalDeactivateEvent;
import com.googlemail.mcdjuady.itemeffects.event.GlobalUpdateEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 *
 * @author Max
 */
public class PlayerEffects {

    private final static Pattern effectPattern = Pattern.compile("^\\|\\w+((!(\\w+)=(((-|\\+)?\\d+(\\.\\d+)?)|\\w+))+)*\\|");
    private final static Pattern effectNamePattern = Pattern.compile("^\\|\\w+(!|\\|)");

    private final Player player;

    private final Map<String, Effect> globalEffects;
    private final Map<String, List<Effect>> globalEffectList;
    private final Map<String, EffectData> globalEffectData;
    private final Map<Integer, List<Effect>> slotEffects;
    private final Map<Integer, ItemStack> storedInventory;
    private final Map<Integer, ItemStack> upToDateInventory;
    private final Map<Class<? extends Effect>, List<Effect>> effectCache;

    private boolean disabled;

    public PlayerEffects(Player player) {
        this.player = player;
        globalEffects = new HashMap<>();
        globalEffectData = new HashMap<>();
        effectCache = new HashMap<>();
        storedInventory = new HashMap<>();
        globalEffectList = new HashMap<>();
        slotEffects = new HashMap<>();
        upToDateInventory = new HashMap<>();
        disabled = false;
        new DelayedInventoryUpdate(this, false).runTaskLater(ItemEffects.getInstance(), 1);
    }

    public<T extends EffectData> T getGlobalData(Effect effect) {
        return getGlobalData(effect.getEffectName());
    }

    public<T extends EffectData> T getGlobalData(String effectName) {
        return (T)globalEffectData.get(effectName);
    }

    public List<Effect> getEffectsForClass(Class<? extends Effect> effectClass) {
        List<Effect> list = effectCache.get(effectClass);
        return list == null ? new ArrayList<Effect>() : new ArrayList<>(list);
    }

    public List<Effect> getGlobalEffects() {
        return new ArrayList<>(globalEffects.values());
    }
    
    public final void updateInventory() {
        Map<Integer, ItemFilter> slots = ItemEffects.getInstance().getEffectSlots();
        PlayerInventory inv = player.getInventory();
        for (int i : slots.keySet()) {
            ItemStack newItem = inv.getItem(i);
            ItemStack oldItem = storedInventory.get(i);
            if (((newItem == null || newItem.getType() == Material.AIR) && (oldItem == null || oldItem.getType() == Material.AIR)) || (newItem != null && oldItem != null && newItem.equals(oldItem))) {
                continue;
            }
            ItemFilter filter = slots.get(i);
            if (oldItem != null && filter.isValid(oldItem)) {
                deactivateItem(i);
                upToDateInventory.put(i, null);
                storedInventory.put(i, null);
            }
            if (newItem != null && filter.isValid(newItem)) {
                storedInventory.put(i, newItem.clone());
                upToDateInventory.put(i, newItem);
                activateItem(i);
            }
        }
        updateItemInHand();
    }

    public final void deactivateAll() {
        Set<ItemStack> items = new HashSet<>(storedInventory.values());
        for (ItemStack item : items) {
            deactivateItem(item);
        }
        //clean up left over global effects
        List<List<Effect>> gList = new ArrayList<>(globalEffectList.values());
        for (List<Effect> gEffects : gList) {
            if (gEffects == null || gEffects.isEmpty()) {
                continue;
            }
            List<Effect> list = new ArrayList<>(gEffects);
            for (Effect effect : list) {
                removeGlobalEffect(effect);
            }
        }
    }

    public final void updateItemInHand() {
        ItemStack newItem = player.getItemInHand();
        ItemStack oldItem = storedInventory.get(-1);
        if (((newItem == null || newItem.getType() == Material.AIR) && (oldItem == null || oldItem.getType() == Material.AIR)) || (newItem != null && oldItem != null && newItem.equals(oldItem))) {
            return;
        }
        ItemFilter filter = ItemEffects.getInstance().getInHandFilter();
        if (oldItem != null && filter.isValid(oldItem)) {
            deactivateItem(-1);
            upToDateInventory.put(-1, null);
            storedInventory.put(-1, null);
        }
        if (newItem != null && filter.isValid(newItem)) {
            storedInventory.put(-1, newItem.clone());
            upToDateInventory.put(-1, newItem);
            activateItem(-1);
        }
    }

    public final void activateItem(int slot) {
        //TODO rewrite so ItemActivateEvent follows event priorities
        if (!storedInventory.containsKey(slot)) {
            return;
        }
        ItemStack item = upToDateInventory.get(slot);
        List<String> lore = item.getItemMeta().getLore();
        if (lore == null || lore.isEmpty()) {
            return;
        }
        List<Effect> itemEffectsList = new ArrayList<>();
        EffectManager manager = ItemEffects.getInstance().getEffectManager();
        for (String info : lore) {
            info = Util.unhideString(info);
            if (effectPattern.matcher(info).find()) {
                Matcher nameMatcher = effectNamePattern.matcher(info);
                if (nameMatcher.find()) {
                    String effectName = nameMatcher.group();
                    effectName = effectName.substring(1, effectName.length() - 1);
                    Effect effect = manager.createEffect(effectName, info, this, slot);
                    if (effect == null) {
                        continue;
                    }
                    itemEffectsList.add(effect);
                }
            }
        }
        slotEffects.put(slot, itemEffectsList);
        ItemActivateEvent event = new ItemActivateEvent(player, item);
        for (Effect e : itemEffectsList) {
            manager.fireEvent(this, e, event);
        }
        if (event.isCancelled()) {
            deactivateItem(slot);
            return;
        }
        for (Effect e : itemEffectsList) {
            if (e.isGlobal()) {
                addGlobalEffect(e);
            } else {
                cacheEffect(e);
            }
        }
    }

    public final void activateItem(ItemStack item) {
        if (item == null) {
            return;
        }
        for (Entry<Integer, ItemStack> entry : upToDateInventory.entrySet()) {
            if (item.equals(entry.getValue())) {
                activateItem(entry.getKey());
                return;
            }
        }
    }

    public void disable() {
        for (List<Effect> effects : slotEffects.values()) {
            for (Effect effect : effects) {
                if (effect.isGlobal()) {
                    continue; //globals are handled later
                }
                if (!effect.ignoresDisabled()) {
                    ItemEffects.getInstance().getEffectManager().fireEvent(this, effect, new ItemDeactivateEvent(player, effect.getItem()));
                }
            }
        }
        for (Effect effect : globalEffects.values()) {
            if (!effect.ignoresDisabled()) {
                ItemEffects.getInstance().getEffectManager().fireEvent(this, effect, new GlobalDeactivateEvent(player, getGlobalData(effect)));
            }
        }
        disabled = true;
    }

    public void enable() {
        disabled = false;
        for (List<Effect> effects : slotEffects.values()) {
            for (Effect effect : effects) {
                if (effect.isGlobal()) {
                    continue; //globals are handled later
                }
                if (!effect.ignoresDisabled()) {
                    ItemEffects.getInstance().getEffectManager().fireEvent(this, effect, new ItemActivateEvent(player, effect.getItem()));
                }
            }
        }
        for (Effect effect : globalEffects.values()) {
            if (!effect.ignoresDisabled()) {
                ItemEffects.getInstance().getEffectManager().fireEvent(this, effect, new GlobalActivateEvent(player, getGlobalData(effect)));
            }
        }
    }

    public void deactivateItem(int slot) {
        if (!storedInventory.containsKey(slot)) {
            return;
        }
        ItemStack item = upToDateInventory.get(slot);
        List<Effect> itemEffectsList = slotEffects.remove(slot);
        if (itemEffectsList == null) {
            return;
        }
        ItemDeactivateEvent event = new ItemDeactivateEvent(player, item);
        EffectManager manager = ItemEffects.getInstance().getEffectManager();
        for (Effect e : itemEffectsList) {
            manager.fireEvent(this, e, event);
            if (e.isGlobal()) {
                removeGlobalEffect(e);
            } else {
                uncacheEffect(e);
            }
        }
    }

    public void deactivateItem(ItemStack item) {
        if (item == null) {
            return;
        }
        for (Entry<Integer, ItemStack> entry : upToDateInventory.entrySet()) {
            if (item.equals(entry.getValue())) {
                deactivateItem(entry.getKey());
                return;
            }
        }
    }

    public void removeGlobalEffect(Effect effect) {
        if (!effect.isGlobal()) {
            return;
        }
        String effectName = effect.getEffectName();
        List<Effect> effectList = globalEffectList.get(effectName);
        if (effectList == null || !effectList.contains(effect)) {
            return;
        }
        effectList.remove(effect);
        if (effectList.isEmpty()) {
            Effect globalEffect = globalEffects.remove(effectName);
            ItemEffects.getInstance().getEffectManager().fireEvent(this, globalEffect, new GlobalDeactivateEvent(player, globalEffectData.get(effectName)));
            globalEffectData.remove(effectName);
            uncacheEffect(globalEffect);
            return;
        }

        if (effect.recalculateGlobal()) {
            EffectData data = null;
            for (Effect e : effectList) {
                if (data == null) {
                    data = e.getOwnEffectData().clone();
                } else {
                    data.combine(e.getOwnEffectData());
                }
            }
            globalEffectData.put(effectName, data);
            ItemEffects.getInstance().getEffectManager().fireEvent(this, globalEffects.get(effectName), new GlobalUpdateEvent(player, data));
        } else {
            EffectData globalData = globalEffectData.get(effectName);
            globalData.remove(effect.getOwnEffectData());
            ItemEffects.getInstance().getEffectManager().fireEvent(this, globalEffects.get(effectName), new GlobalUpdateEvent(player, globalData));
        }

    }

    public void removeEffect(int slot, String effectName) {
        List<Effect> slotList = slotEffects.get(slot);
        if (slotList == null || slotList.isEmpty()) {
            return;
        }
        for (Effect effect : slotList) {
            if (effect.getEffectName().equalsIgnoreCase(effectName)) {
                effect.remove();
                break;
            }
        }
        new DelayedInventoryUpdate(this, slot == EffectManager.INHANDSLOT).runTaskLater(ItemEffects.getInstance(), 1);
    }
    
    public void addGlobalEffect(Effect effect) {
        if (!effect.isGlobal()) {
            return;
        }
        String effectName = effect.getEffectName();
        List<Effect> effectList = globalEffectList.get(effectName);
        EffectData globalData = globalEffectData.get(effectName);
        if (globalData != null && globalData.isSimmilar(effect.getOwnEffectData())) {
            globalData.combine(effect.getOwnEffectData());
            effectList.add(effect);
            ItemEffects.getInstance().getEffectManager().fireEvent(this, globalEffects.get(effectName), new GlobalUpdateEvent(player, globalData));
        } else {
            effectList = new ArrayList<>();
            effectList.add(effect);
            globalEffectList.put(effectName, effectList);
            globalEffects.put(effectName, effect);
            globalData = effect.getOwnEffectData().clone();
            globalEffectData.put(effectName, globalData);
            ItemEffects.getInstance().getEffectManager().fireEvent(this, effect, new GlobalActivateEvent(player, globalData));
            cacheEffect(effect);
        }
    }
    
    public void updateGlobalEffects() {
        EffectManager manager = ItemEffects.getInstance().getEffectManager();
        for (String effectName : globalEffects.keySet()) {
            Effect effect = globalEffects.get(effectName);
            GlobalUpdateEvent event = new GlobalUpdateEvent(player, getGlobalData(effectName));
            manager.fireEvent(this, effect, event);
        }
    }

    private void cacheEffect(Effect effect) {
        List<Effect> effectList = effectCache.get(effect.getClass());
        if (effectList == null) {
            effectList = new ArrayList<>();
            effectCache.put(effect.getClass(), effectList);
        }
        effectList.add(effect);
    }

    private void uncacheEffect(Effect effect) {
        List<Effect> effectList = effectCache.get(effect.getClass());
        if (effectList == null) {
            return;
        }
        effectList.remove(effect);
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isDisabled() {
        return disabled;
    }
}
