/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.effect;

import com.googlemail.mcdjuady.itemeffects.Util;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.projectiles.ProjectileSource;

/**
 *
 * @author Max
 */
public abstract class Effect {

    public final static char dataSperator = '!';
    private final static Pattern keyPattern = Pattern.compile("%(\\+)?\\w+%");
    public final static Pattern dataPattern = Pattern.compile("^\\w+=(((-|\\+)?\\d+(\\.\\d+)?)|\\w+)$");

    private final static Set<Class<? extends Effect>> initializedClasses = new HashSet<>();
    private final static Map<Class<? extends Effect>, EffectDataHelper[]> helpers = new HashMap<>();
    private final static Map<Class<? extends Effect>, Constructor<? extends EffectData>> dataConstructor = new HashMap<>();

    private final String effectId;
    private final String effectName;
    private final String humanName;
    private final boolean global;
    private final boolean recalculateGlobal;
    private final boolean ignoresDisabled;
    private final ItemStack item;

    private EffectData data;

    public Effect(ConfigurationSection effectConfig, ItemStack item, String effectInfo) throws InvalidConfigurationException {
        this.effectId = effectConfig.getString("EffectId");
        this.effectName = effectConfig.getName();
        this.humanName = effectConfig.getString("Name");
        this.item = item;
        EffectOptions options = this.getClass().getAnnotation(EffectOptions.class);
        if (options == null) {
            throw new InvalidConfigurationException("Missing options annotations for Effect " + effectName);
        }
        this.global = options.global();
        this.recalculateGlobal = options.recalculateGlobal();
        this.ignoresDisabled = options.ignoreDisabled();
        initStatic();
        Constructor<? extends EffectData> constructor = getDataConstructor();
        if (constructor != null) {
            try {
                this.data = constructor.newInstance(getDataHelpers(), effectInfo, effectConfig.getConfigurationSection("EffectConfig"));
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to instantiate DataClass " + options.dataClass().getSimpleName() + " for effect " + effectName + "! Using default EffectData class", ex);
            }
        }
        //use default data if data is not set
        if (data == null) {
            this.data = new EffectData(getDataHelpers(), effectInfo, effectConfig.getConfigurationSection("EffectConfig"));
        }

    }

    private void initStatic() {
        if (initializedClasses.contains(this.getClass())) {
            return;
        }
        initializedClasses.add(this.getClass());
        EffectOptions options = this.getClass().getAnnotation(EffectOptions.class);
        if (options == null) {
            return;
        }
        EffectDataHelper[] helperArray = new EffectDataHelper[options.dataOptions().length];
        for (int i = 0; i < helperArray.length; i++) {
            helperArray[i] = new EffectDataHelper(options.dataOptions()[i]);
        }
        helpers.put(this.getClass(), helperArray);
        try {
            Constructor< ? extends EffectData> myDataConstructor = options.dataClass().getConstructor(EffectDataHelper[].class, String.class, ConfigurationSection.class);
            dataConstructor.put(this.getClass(), myDataConstructor);
        } catch (NoSuchMethodException | SecurityException ex) {
            Bukkit.getLogger().log(Level.WARNING, "Invalid DataClass " + options.dataClass().getSimpleName() + " for effect " + effectName + "! Using default EffectData class", ex);
        }
    }

    public final void inscribe() {
        if (getItem() == null) {
            return;
        }
        String effectInfo = "|" + effectName + data.toString() + "|";
        Bukkit.getLogger().log(Level.INFO, "EffectInfo {0}", effectInfo);
        Matcher matcher = keyPattern.matcher(humanName);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String match = matcher.group();
            String key = match.substring(1, match.length() - 1);
            boolean showSign = key.charAt(0) == '+';
            if (showSign) {
                key = key.substring(1);
            }
            if (data.contains(key)) {
                Object value = data.get(key);
                if (showSign && value instanceof Number && Math.signum(((Number) value).doubleValue()) == 1) {
                    sb.append('+'); //Don't need to append '-' sign, since Number.toString() will return the minus in front
                }
                matcher.appendReplacement(sb, Matcher.quoteReplacement(value.toString()));
            } else {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(match));
            }
        }
        matcher.appendTail(sb);
        String loreString = Util.hideString(effectInfo) + ChatColor.translateAlternateColorCodes('$', sb.toString());
        ItemMeta meta = getItem().getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.add(loreString);
        meta.setLore(lore);
        getItem().setItemMeta(meta);
    }

    public final boolean isGlobal() {
        return global;
    }
    
    public final boolean recalculateGlobal() {
        return recalculateGlobal;
    }

    public final boolean ignoresDisabled() {
        return ignoresDisabled;
    }
    
    public final String getEffectName() {
        return effectName;
    }

    public final EffectData getEffectData() {
        return data;
    }

    //Inaccurate for globalEffects
    public final ItemStack getItem() {
        return item;
    }
    
    public static Entity getAttacker(EntityDamageByEntityEvent e) {
        Entity ent = e.getDamager();
        if (ent instanceof Projectile) {
            ProjectileSource source = ((Projectile)ent).getShooter();
            if (source instanceof Entity) {
                ent = (Entity)source;
            }
        }
        return ent;
    }

    private EffectDataHelper[] getDataHelpers() {
        initStatic();
        return helpers.get(this.getClass());
    }

    private Constructor<? extends EffectData> getDataConstructor() {
        initStatic();
        return dataConstructor.get(this.getClass());
    }

    @Override
    public int hashCode() {
        int hashCode = 7;
        hashCode = 37 * hashCode + data.hashCode();
        hashCode = 37 * hashCode + effectId.hashCode();
        hashCode = 37 * hashCode + effectName.hashCode();
        hashCode = 37 * hashCode + (global ? 1 : 0);
        hashCode = 37 * hashCode + humanName.hashCode();
        hashCode = 37 * hashCode + (item == null ? 0 : item.hashCode());
        hashCode = 37 * hashCode + (recalculateGlobal ? 1 : 0);
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Effect other = (Effect) obj;
        if (!Objects.equals(this.effectId, other.effectId)) {
            return false;
        }
        if (!Objects.equals(this.effectName, other.effectName)) {
            return false;
        }
        if (!Objects.equals(this.humanName, other.humanName)) {
            return false;
        }
        if (this.global != other.global) {
            return false;
        }
        if (this.recalculateGlobal != other.recalculateGlobal) {
            return false;
        }
        if (!Objects.equals(this.item, other.item)) {
            return false;
        }
        return Objects.equals(this.data, other.data);
    }
}
