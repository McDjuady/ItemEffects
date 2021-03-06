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
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;

/**
 *
 * @author Max
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EffectHandler {
    
    Class<? extends Event>[] events() default {};
    EventPriority priority() default EventPriority.NORMAL;
    boolean ignoreCancelled() default false;
    
}
