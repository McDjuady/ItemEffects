/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.effect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Max
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EffectDataOption {
    String key();
    String value() default "0";
    Class<?> dataClass() default Double.class;
    Class<? extends EffectDataCombiner> combiner() default EffectDataAddCombiner.class;
    boolean canEnchant() default true;
}
