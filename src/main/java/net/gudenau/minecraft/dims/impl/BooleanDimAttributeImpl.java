package net.gudenau.minecraft.dims.impl;

import net.gudenau.minecraft.dims.api.v0.attribute.BooleanDimAttribute;
import net.minecraft.util.Identifier;

import static net.gudenau.minecraft.dims.Dims.MOD_ID;

public final class BooleanDimAttributeImpl implements BooleanDimAttribute{
    private final boolean value;
    private final Identifier id;
    
    BooleanDimAttributeImpl(boolean value){
        this.value = value;
        id = new Identifier(MOD_ID, value ? "true" : "false");
    }
    
    @Override
    public boolean getBoolean(){
        return value;
    }
    
    @Override
    public Identifier getId(){
        return id;
    }
}
