/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Max
 */
public class Util {

    public static String hideString(String str) {
        char[] data = new char[str.length() * 2];
        for (int i = 0; i < data.length; i += 2) {
            data[i] = ChatColor.COLOR_CHAR;
            data[i + 1] = str.charAt(i / 2);
        }
        return new String(data);
    }

    public static String unhideString(String str) {
        return str.replaceAll(String.valueOf(ChatColor.COLOR_CHAR), "");
    }

    private final static Pattern dataPattern = Pattern.compile("^\\|(\\w+(\\.\\d)?(!\\w+(\\.\\d)?)*)\\|");

    public static List<EffectData> getItemEffects(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        List<String> lore = item.getItemMeta().getLore();
        if (lore == null || lore.isEmpty()) {
            return null;
        }
        List<EffectData> retList = new ArrayList<>();
        for (String string : lore) {
            EffectData data = getEffectData(string);
            if (data != null) {
                retList.add(data);
            }
        }
        Bukkit.getLogger().log(Level.INFO, "ItemEffects {0}", retList.toString());
        return retList;
    }

    public static EffectData getEffectData(String string) {
        string = Util.unhideString(string);
        Bukkit.getLogger().log(Level.INFO, "getData() {0}", string);
        Matcher matcher = dataPattern.matcher(string);
        if (!matcher.find()) {
            Bukkit.getLogger().log(Level.INFO, "missmatch");
            return null;
        }
        String info = matcher.group().substring(1);
        info = info.substring(0, info.length() - 1);
        String[] split = info.split("!");
        Effect effect = ItemEffects.getInstance().getEffectManager().getEffect(split[0]);
        return effect != null ? new EffectData(effect,split) : null;
    }
}
