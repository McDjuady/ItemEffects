/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Max
 */
public class ItemUnequipEvent extends PlayerEvent {

    private final static HandlerList handlers = new HandlerList();

    private final ItemStack item;
    private final int slot;

    public ItemUnequipEvent(Player player, ItemStack item, int slot) {
        super(player);
        this.item = item;
        this.slot = slot;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * @return the item
     */
    public ItemStack getItem() {
        return item;
    }

    /**
     * @return the slot
     */
    public int getSlot() {
        return slot;
    }
}
