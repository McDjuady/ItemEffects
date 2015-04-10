/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.effect;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.googlemail.mcdjuady.itemeffects.EffectManager;
import com.googlemail.mcdjuady.itemeffects.ItemEffects;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

/**
 *
 * @author Max
 */
public class InventoryPacketUpdateListener extends PacketAdapter {

    private final ItemEffects plugin;
    
    public InventoryPacketUpdateListener(ItemEffects plugin) {
        super(plugin, ListenerPriority.MONITOR, PacketType.Play.Server.SET_SLOT);
        this.plugin = plugin;
    }

    @Override
    public void onPacketSending(PacketEvent pe) {
        if (pe.getPacketType() != PacketType.Play.Server.SET_SLOT) {
            return;
        }
        PacketContainer container = pe.getPacket();
        if (container.getIntegers().read(0) != 0) {
            return;
        }
        Player player = pe.getPlayer();
        PlayerInventory inv = player.getInventory();
        int slot = pe.getPacket().getIntegers().read(1);
        if (slot < 5) {
            return;
        }
        //convert to bukkit slots
        if (slot > 4 && slot < 9) {
            slot = 39 - (slot - 5);
        } else if (slot > 35) {
            slot -= 35;
        }
        //set to -1 if in hand
        if (slot == inv.getHeldItemSlot()) {
            slot = EffectManager.INHANDSLOT;
        }
        if (plugin.getEffectSlots().containsKey(slot)) {
            plugin.getUpdateTask().scheduleUpdate(player, slot == EffectManager.INHANDSLOT);
        }
    }
}
