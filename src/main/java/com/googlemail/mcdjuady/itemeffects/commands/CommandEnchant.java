/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.commands;

import com.googlemail.mcdjuady.itemeffects.effect.Effect;
import com.googlemail.mcdjuady.itemeffects.ItemEffects;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
        Effect effect = ItemEffects.getInstance().getEffectManager().enchant(args[0], itemInHand, args);
        if (effect == null) {
            sender.sendMessage("Failed to enchant! See console for details");
        }
        return true;
    }

}
