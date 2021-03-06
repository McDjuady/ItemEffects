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
public class EffectDataAddCombiner implements EffectDataCombiner<Number>{
    
    @Override
    public Number combine(Number o1, Number o2) {
        return o1.doubleValue() + o2.doubleValue(); //use double value since it's the highest percission
    }

    @Override
    public Number remove(Number t1, Number t2) {
        return t1.doubleValue() - t2.doubleValue();
    }
    
    
}
