package net.gudenau.minecraft.dims.impl;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.GameRules;

/**
 * A game rule type that allows for per-dimension definition of game rules.
 *
 * TODO Implement everything in this.
 *
 * @since 0.0.1
 */
public final class DimensionGameRules extends GameRules{
    public static DimensionGameRules fromNbt(NbtCompound rules, GameRules parentRules){
        return new DimensionGameRules();
    }
    
    @Override
    public <T extends Rule<T>> T get(Key<T> key){
        return super.get(key);
    }
    
    @Override
    public NbtCompound toNbt(){
        return new NbtCompound();
    }
}
