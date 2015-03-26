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
public interface EffectDataCombiner<T> {
    public T combine(T t1, T t2);
    public T remove(T t1, T t2);
}
