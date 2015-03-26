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
}
