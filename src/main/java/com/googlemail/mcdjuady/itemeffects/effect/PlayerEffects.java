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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private final Map<ItemStack, List<Effect>> itemEffects;
    private final Map<Integer, ItemStack> itemInventory;
    private final Map<Class<? extends Effect>, List<Effect>> effectCache;

    public PlayerEffects(Player player) {
        this.player = player;
        globalEffects = new HashMap<>();
        globalEffectData = new HashMap<>();
        itemEffects = new HashMap<>();
        effectCache = new HashMap<>();
        itemInventory = new HashMap<>();
        globalEffectList = new HashMap<>();
        new DelayedInventoryUpdate(this, false).runTaskLater(ItemEffects.getInstance(), 1);
    }

    public EffectData getGlobalData(Effect effect) {
        return getGlobalData(effect.getEffectName());
    }

    public EffectData getGlobalData(String effectName) {
        return globalEffectData.get(effectName);
    }

    public List<Effect> getEffectsForClass(Class<? extends Effect> effectClass) {
        return effectCache.get(effectClass);
    }

    public final void updateInventory() {
        Map<Integer, ItemFilter> slots = ItemEffects.getInstance().getEffectSlots();
        PlayerInventory inv = player.getInventory();
        for (int i : slots.keySet()) {
            ItemStack newItem = inv.getItem(i);
            ItemStack oldItem = itemInventory.get(i);
            if (Objects.equals(newItem, oldItem) || ((newItem == null || newItem.getType() == Material.AIR) && (oldItem == null || oldItem.getType() == Material.AIR))) {
                continue;
            }
            ItemFilter filter = slots.get(i);
            if (oldItem != null && filter.isValid(oldItem)) {
                itemInventory.put(i, null);
                deactivateItem(oldItem);
            }
            if (newItem != null && filter.isValid(newItem)) {
                itemInventory.put(i, newItem.clone());
                activateItem(newItem);
            }
        }
        updateItemInHand();
    }

    public final void deactivateAll() {
        for (ItemStack item : itemEffects.keySet()) {
            deactivateItem(item);
        }
    }

    public final void updateItemInHand() {
        ItemStack newItem = player.getItemInHand();
        ItemStack oldItem = itemInventory.get(-1);
        if (Objects.equals(newItem, oldItem) || ((newItem == null || newItem.getType() == Material.AIR) && (oldItem == null || oldItem.getType() == Material.AIR))) {
            return;
        }
        ItemFilter filter = ItemEffects.getInstance().getInHandFilter();
        if (oldItem != null && filter.isValid(oldItem)) {
            itemInventory.put(-1, null);
            deactivateItem(oldItem);
        }
        if (newItem != null && filter.isValid(newItem)) {
            itemInventory.put(-1, newItem.clone());
            activateItem(newItem);
        }
    }

    public final void activateItem(ItemStack item) {
        if (itemEffects.containsKey(item) || !item.hasItemMeta()) {
            return;
        }
        List<String> lore = item.getItemMeta().getLore();
        if (lore.isEmpty()) {
            return;
        }
        item = item.clone(); //we have to clone here since bukkit sometimes messes with the amount
        List<Effect> itemEffectsList = new ArrayList<>();
        EffectManager manager = ItemEffects.getInstance().getEffectManager();
        for (String info : lore) {
            info = Util.unhideString(info);
            if (effectPattern.matcher(info).find()) {
                Matcher nameMatcher = effectNamePattern.matcher(info);
                if (nameMatcher.find()) {
                    String effectName = nameMatcher.group();
                    effectName = effectName.substring(1, effectName.length() - 1);
                    Effect effect = manager.createEffect(effectName, item, info);
                    if (effect == null) {
                        continue;
                    }
                    itemEffectsList.add(effect);
                    if (effect.isGlobal()) {
                        addGlobalEffect(effect);
                    } else {
                        cacheEffect(effect);
                    }
                }
            }
        }
        itemEffects.put(item, itemEffectsList);
        ItemActivateEvent event = new ItemActivateEvent(player, item);
        for (Effect e : itemEffectsList) {
            manager.fireEvent(this, e, event);
        }
    }

    public void deactivateItem(ItemStack item) {
        if (!itemEffects.containsKey(item)) {
            return;
        }
        List<Effect> itemEffectsList = itemEffects.remove(item);
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

    private void removeGlobalEffect(Effect effect) {
        if (!effect.isGlobal()) {
            return;
        }
        String effectName = effect.getEffectName();
        List<Effect> effectList = globalEffectList.get(effectName);
        if (effectList != null) {
            effectList.remove(effect);
        }
        if (effectList == null || effectList.isEmpty()) {
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
                    data = e.getEffectData().clone();
                } else {
                    data.combine(e.getEffectData());
                }
            }
            globalEffectData.put(effectName, data);
            ItemEffects.getInstance().getEffectManager().fireEvent(this, globalEffects.get(effectName), new GlobalUpdateEvent(player, data));
        } else {
            EffectData globalData = globalEffectData.get(effectName);
            globalData.remove(effect.getEffectData());
            ItemEffects.getInstance().getEffectManager().fireEvent(this, globalEffects.get(effectName), new GlobalUpdateEvent(player, globalData));
        }

    }

    private void addGlobalEffect(Effect effect) {
        if (!effect.isGlobal()) {
            return;
        }
        String effectName = effect.getEffectName();
        List<Effect> effectList = globalEffectList.get(effectName);
        EffectData globalData = globalEffectData.get(effectName);
        if (globalData != null && globalData.isSimmilar(effect.getEffectData())) {
            globalData.combine(effect.getEffectData());
            effectList.add(effect);
            ItemEffects.getInstance().getEffectManager().fireEvent(this, globalEffects.get(effectName), new GlobalUpdateEvent(player, globalData));
        } else {
            effectList = new ArrayList<>();
            effectList.add(effect);
            globalEffectList.put(effectName, effectList);
            globalEffects.put(effectName, effect);
            globalData = effect.getEffectData().clone();
            globalEffectData.put(effectName, globalData);
            ItemEffects.getInstance().getEffectManager().fireEvent(this, effect, new GlobalActivateEvent(player, globalData));
            cacheEffect(effect);
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
}
