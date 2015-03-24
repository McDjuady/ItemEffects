/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.commands;

import com.googlemail.mcdjuady.itemeffects.Effect;
import com.googlemail.mcdjuady.itemeffects.EffectData;
import com.googlemail.mcdjuady.itemeffects.ItemEffects;
import com.googlemail.mcdjuady.itemeffects.Util;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author Max
 */
public class CommandEnchant implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player) || args.length < 1) {
            return false;
        }
        Player player = (Player)sender;
        ItemStack itemInHand = player.getItemInHand();
        if (itemInHand == null || itemInHand.getType() == Material.AIR) {
            sender.sendMessage("No item in hand");
            return true;
        }
        Effect effect = ItemEffects.getInstance().getEffectManager().getEffect(args[0]);
        if (effect == null) {
            sender.sendMessage("Invalid effect");
            return true;
        }
        String[] data = effect.getDefaultData();
        if (args.length > 1) {
            for (int i = 0; i < data.length && i < args.length - 1; i++) {
                data[i] = args[i+1];
            }
        }
        String effectInfo = "|"+effect.getName();
        if (data.length > 0) {
            for (String string : data) {
                effectInfo += "!"+string;
            }
        }
        effectInfo += "|";
        EffectData effectData = Util.getEffectData(effectInfo);
        List<Double> intData = effectData.getData();
        Bukkit.getLogger().log(Level.INFO, "Format for {0} with {1}", new Object[]{effect.getHumanName(), intData.toString()});
        String name = String.format(effect.getHumanName(),intData.toArray());
        ItemMeta meta = itemInHand.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.add(Util.hideString(effectInfo) + ChatColor.translateAlternateColorCodes('$',name));
        meta.setLore(lore);
        itemInHand.setItemMeta(meta);
        return true;
    }

}
