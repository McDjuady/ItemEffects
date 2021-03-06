/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.effect;

import com.googlemail.mcdjuady.itemeffects.EffectManager;
import com.googlemail.mcdjuady.itemeffects.filter.FilterGroups;
import com.googlemail.mcdjuady.itemeffects.ItemEffects;
import com.googlemail.mcdjuady.itemeffects.filter.ItemFilter;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Max
 */
public class EffectItemListener implements Listener {

    private final static ItemFilter armorFilter = new ItemFilter(FilterGroups.ARMOR);

    private final EffectManager manager;
    private final ItemEffects plugin;

    public EffectItemListener(ItemEffects plugin) {
        this.plugin = plugin;
        this.manager = plugin.getEffectManager();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemInHandChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        plugin.getUpdateTask().scheduleUpdate(player, true); //in hand could be anything, so always check
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.hasItem() && event.useItemInHand() != Event.Result.DENY) {
            ItemStack item = event.getItem();
            if (armorFilter.isValid(item)) { //since only armor can be right clicked into the inventory only check then
                plugin.getUpdateTask().scheduleUpdate(event.getPlayer(), false);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack currentItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();
        if (((currentItem != null && currentItem.getType() != Material.AIR) || (cursorItem != null && cursorItem.getType() != Material.AIR)) && event.getWhoClicked().getType() == EntityType.PLAYER) {
            plugin.getUpdateTask().scheduleUpdate((Player)event.getWhoClicked(), false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemPickup(InventoryPickupItemEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof Player) {
            plugin.getUpdateTask().scheduleUpdate((Player)holder, false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof Player) {
            plugin.getUpdateTask().scheduleUpdate((Player)holder, false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event) {
        plugin.getUpdateTask().scheduleUpdate(event.getPlayer(), true); //only update inHand since drops from within the inventory are already handled in onInventoryClick
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent event) {
        plugin.getUpdateTask().scheduleUpdate(event.getPlayer(), false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent e) {
        if (e.getEntity().getType() == EntityType.PLAYER) {
            Player player = (Player) e.getEntity();
            if (player.getHealth() <= e.getFinalDamage()) {
                PlayerEffects pEffects = manager.getPlayerEffects(player);
                pEffects.deactivateAll();
            }
        }
    }

    //update the inventory on respawn since some plugins may preserve the inventory of the player
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        PlayerEffects pEffects = manager.getPlayerEffects(event.getPlayer());
        pEffects.updateInventory();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        manager.onPlayerJoin(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        manager.onPlayerQuit(event.getPlayer());
    }
}
