/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects;

import com.googlemail.mcdjuady.itemeffects.event.ItemDeselectEvent;
import com.googlemail.mcdjuady.itemeffects.event.ItemEquipEvent;
import com.googlemail.mcdjuady.itemeffects.event.ItemSelectEvent;
import com.googlemail.mcdjuady.itemeffects.event.ItemUnequipEvent;
import java.util.Collection;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 *
 * @author Max
 */
public class EffectListener implements Listener {

    private final EffectManager manager;

    public EffectListener(EffectManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player) {
            ActiveEffects playerEffects = manager.getActiveEffects((Player) e.getEntity());
            manager.fireEvent(playerEffects, e);
        }
        if (e.getDamager() instanceof Player) {
            ActiveEffects playerEffects = manager.getActiveEffects((Player) e.getDamager());
            manager.fireEvent(playerEffects, e);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemHeldChange(PlayerItemHeldEvent e) {
        //update the ActiveEffects
        Player player = e.getPlayer();
        ActiveEffects activeEffects = manager.getActiveEffects(player);

        //fire equip and unequip events
        ItemStack prev = player.getInventory().getItem(e.getPreviousSlot());
        ItemStack current = player.getInventory().getItem(e.getNewSlot());
        if (current == prev || ((current == null || current.getType() == Material.AIR) && (prev == null || prev.getType() == Material.AIR))) {
            return; //No need to update
        }
        ItemFilter filter = ItemEffects.getInstance().getInHandFilter();
        if (filter.isValid(prev)) {
            manager.fireEvent(activeEffects, new ItemDeselectEvent(player, prev));
        }
        activeEffects.updateItemInHand(filter.isValid(current) ? current : null);
        if (filter.isValid(current)) {
            manager.fireEvent(activeEffects, new ItemSelectEvent(player, current));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        //only care if it's a player and a playerInventory
        if (e.isCancelled() || e.getClickedInventory() == null || !(e.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) e.getWhoClicked();
        if (!e.getClickedInventory().equals(player.getInventory())) {
            Bukkit.getLogger().info("Not playerInv");
            return;
        }
        //TODO correctly assign current and prev (drop, take out etc)
        ItemStack prev = e.getCurrentItem(); //old item (if any)
        ItemStack current = e.getCursor();   //new item (if any)
        if (current == prev || ((current == null || current.getType() == Material.AIR) && (prev == null || prev.getType() == Material.AIR))) {
            return; //No need to update
        }
        //special handling for heldItem
        ActiveEffects activeEffects = manager.getActiveEffects(player);
        if (e.getSlot() == player.getInventory().getHeldItemSlot()) {
            ItemFilter filter = ItemEffects.getInstance().getInHandFilter();
            if (filter.isValid(prev)) {
                manager.fireEvent(activeEffects, new ItemDeselectEvent(player, prev));
            }
            activeEffects.updateItemInHand(filter.isValid(current) ? current : null);
            if (filter.isValid(current)) {
                manager.fireEvent(activeEffects, new ItemSelectEvent(player, current));
            }
        } else {
            Map<Integer, ItemFilter> slots = ItemEffects.getInstance().getEffectSlots();
            if (!slots.containsKey(e.getSlot())) {
                return; //Only continue if we can put this item here
            }
            ItemFilter filter = slots.get(e.getSlot());
            if (filter.isValid(prev)) {
                manager.fireEvent(activeEffects, new ItemUnequipEvent(player, prev, e.getSlot()));
            }
            activeEffects.updateSlot(e.getSlot(), filter.isValid(current) ? current : null);
            if (filter.isValid(current)) {
                manager.fireEvent(activeEffects, new ItemEquipEvent(player, current, e.getSlot()));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        PlayerInventory inv = player.getInventory();
        ActiveEffects activeEffects = manager.getActiveEffects(player);
        if (activeEffects.isEmpty()) {
            Bukkit.getLogger().info("Empty effects");
            return;
        }
        Map<Integer,ItemFilter> activeSlots = ItemEffects.getInstance().getEffectSlots();
        for (int i : activeSlots.keySet()) {
            ItemStack item = inv.getItem(i);
            if (activeSlots.get(i).isValid(item)) {
                manager.fireEvent(activeEffects, new ItemEquipEvent(player, item, i));
            }
        }
        if (ItemEffects.getInstance().getInHandFilter().isValid(player.getItemInHand())) {
            manager.fireEvent(activeEffects, new ItemSelectEvent(player, player.getItemInHand()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent e) {
        manager.onPlayerQuit(e.getPlayer());
    }
}
