/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.commands;

import com.googlemail.mcdjuady.itemeffects.EffectManager;
import com.googlemail.mcdjuady.itemeffects.ItemEffects;
import com.googlemail.mcdjuady.itemeffects.effect.DelayedInventoryUpdate;
import com.googlemail.mcdjuady.itemeffects.effect.Effect;
import com.googlemail.mcdjuady.itemeffects.effect.PlayerEffects;
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
public class CommandItemEffects implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return false;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            ItemEffects.getInstance().reloadConfig();
            sender.sendMessage("Config reloaded successfully!");
            return true;
        }
        if (args[0].equalsIgnoreCase("enchant") && args.length >= 2) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be executed by a player");
                return false;
            }
            Player player = (Player) sender;
            ItemStack itemInHand = player.getItemInHand();
            if (itemInHand == null || itemInHand.getType() == Material.AIR) {
                sender.sendMessage("No item in hand");
                return true;
            }
            Effect effect = ItemEffects.getInstance().getEffectManager().enchant(args[1], player, EffectManager.INHANDSLOT, args);
            if (effect == null) {
                sender.sendMessage("Failed to enchant! See console for details");
            }
            new DelayedInventoryUpdate(ItemEffects.getInstance().getEffectManager().getPlayerEffects(player), true).runTaskLater(ItemEffects.getInstance(), 1);
            return true;
        }
        if (args[0].equalsIgnoreCase("remove") && args.length == 2) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be executed by a player");
                return false;
            }
            Player player = (Player) sender;
            ItemStack itemInHand = player.getItemInHand();
            if (itemInHand == null || itemInHand.getType() == Material.AIR) {
                sender.sendMessage("No item in hand");
                return true;
            }
            PlayerEffects effects = ItemEffects.getInstance().getEffectManager().getPlayerEffects(player);
            effects.removeEffect(EffectManager.INHANDSLOT, args[1]);
            return true;
        }
        return false;
    }

}
