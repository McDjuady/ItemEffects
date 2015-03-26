/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;

/**
 *
 * @author Max
 */
public enum FilterGroups {

    HELMET(Material.DIAMOND_HELMET, Material.GOLD_HELMET, Material.IRON_HELMET, Material.CHAINMAIL_HELMET, Material.LEATHER_HELMET),
    CHEST(Material.DIAMOND_CHESTPLATE, Material.GOLD_CHESTPLATE, Material.IRON_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.LEATHER_CHESTPLATE),
    LEGS(Material.DIAMOND_LEGGINGS, Material.GOLD_LEGGINGS, Material.IRON_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.LEATHER_LEGGINGS),
    BOOTS(Material.DIAMOND_BOOTS, Material.GOLD_BOOTS, Material.IRON_BOOTS, Material.CHAINMAIL_BOOTS, Material.LEATHER_BOOTS),
    ARMOR(HELMET, CHEST, LEGS, BOOTS),
    SWORD(Material.DIAMOND_SWORD, Material.GOLD_SWORD, Material.IRON_SWORD, Material.STONE_SWORD, Material.WOOD_SWORD),
    BOW(Material.BOW), AXE(Material.DIAMOND_AXE, Material.GOLD_AXE, Material.IRON_AXE, Material.WOOD_AXE),
    HOE(Material.DIAMOND_HOE, Material.GOLD_AXE, Material.IRON_HOE, Material.WOOD_HOE),
    PICKAXE(Material.DIAMOND_PICKAXE, Material.GOLD_PICKAXE, Material.IRON_PICKAXE, Material.WOOD_PICKAXE),
    SPADE(Material.DIAMOND_SPADE, Material.GOLD_SPADE, Material.IRON_SPADE, Material.WOOD_SPADE),
    ROD(Material.FISHING_ROD),
    TOOL(AXE, HOE, PICKAXE, SPADE, ROD),
    ANY(ARMOR, TOOL, BOW, SWORD);

    private Material[] valid;
    private FilterGroups[] children;

    private FilterGroups(Material... valid) {
        this.valid = valid;
    }

    private FilterGroups(FilterGroups... children) {
        this.children = children;
    }

    public static FilterGroups getGroup(String target) {
        try {
            return valueOf(target);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public List<Material> getValidMaterials() {
        if (ANY == this) {
            return Arrays.asList(Material.values());
        }
        List<Material> returnList = new ArrayList<>();
        if (children != null) {
            for (FilterGroups type : children) {
                returnList.addAll(type.getValidMaterials());
            }
        }
        if (valid != null) {
            Collections.addAll(returnList, valid);
        }
        return returnList;
    }

}
