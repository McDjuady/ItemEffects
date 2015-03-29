/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.effect;

import com.googlemail.mcdjuady.itemeffects.filter.FilterGroups;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Max
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EffectOptions {
    EffectDataOption[] dataOptions();
    FilterGroups[] target() default FilterGroups.ANY;
    Class<? extends EffectData> dataClass() default EffectData.class;
    boolean global() default false; //determines if the effect is fired once or per item
    boolean recalculateGlobal() default false; //determines if the combined data should be recalculatet every time data is removed from the globalEffect (global only)
}
