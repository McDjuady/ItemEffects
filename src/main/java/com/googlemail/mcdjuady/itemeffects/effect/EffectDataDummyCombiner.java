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
public class EffectDataDummyCombiner implements EffectDataCombiner<Object>{

    @Override
    public Object combine(Object t1, Object t2) {
        return t1;
    }

    @Override
    public Object remove(Object t1, Object t2) {
        return t1;
    }
    
}
