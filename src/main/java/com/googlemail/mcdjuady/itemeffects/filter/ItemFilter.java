/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Max
 */
public class ItemFilter {

    private final Set<Material> valid;
    
    public ItemFilter(List<String> filter) {
        valid = new HashSet<>();
        for (String string : filter) {
            FilterGroups target = FilterGroups.getGroup(string);
            if (target != null) {
                valid.addAll(target.getValidMaterials());
                continue;
            }
            Material mat = Material.getMaterial(string);
            if (mat != null) {
                valid.add(mat);
            }
        }
    }
    
    public ItemFilter(FilterGroups... filter) {
        valid = new HashSet<>();
        for (FilterGroups type : filter) {
            valid.addAll(type.getValidMaterials());
        }
    }
    
    public ItemFilter(Material... mat) {
        valid = new HashSet<>();
        Collections.addAll(valid, mat);
    }
    
    public boolean isValid(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        return valid.contains(item.getType());
    }

    @Override
    public String toString() {
        return "SlotFilter: "+valid.toString();
    }

    
    
}
