package net.gudenau.minecraft.dims.impl.attribute;

import net.gudenau.minecraft.dims.api.v0.attribute.DigitDimAttribute;
import net.minecraft.util.Identifier;

import static net.gudenau.minecraft.dims.Dims.MOD_ID;

public final class DigitDimAttributeImpl implements DigitDimAttribute{
    private final int value;
    private final Identifier id;
    
    public DigitDimAttributeImpl(int value){
        this.value = value;
        id = new Identifier(MOD_ID, "digit" + value);
    }
    
    @Override
    public int getValue(){
        return value;
    }
    
    @Override
    public Identifier getId(){
        return id;
    }
}
