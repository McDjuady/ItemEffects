/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.itemeffects.effect;

/**
 *
 * @author Max
 */
public class EffectDataMaxCombiner implements EffectDataCombiner<Number>{

    @Override
    public Number combine(Number t1, Number t2) {
        return Math.max(t1.doubleValue(), t2.doubleValue());
    }

    @Override
    public Number remove(Number t1, Number t2) {
        return t2;
    }
    
}
