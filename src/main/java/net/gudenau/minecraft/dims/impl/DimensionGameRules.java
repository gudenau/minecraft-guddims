package net.gudenau.minecraft.dims.impl;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.GameRules;

public final class DimensionGameRules extends GameRules{
    public static DimensionGameRules fromNbt(NbtCompound rules, GameRules parentRules){
        return new DimensionGameRules();
    }
    
    @Override
    public <T extends Rule<T>> T get(Key<T> key){
        //TODO
        return super.get(key);
    }
    
    @Override
    public NbtCompound toNbt(){
        return new NbtCompound();
    }
}
