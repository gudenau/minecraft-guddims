package net.gudenau.minecraft.dims.impl.attribute;

import net.gudenau.minecraft.dims.api.v0.attribute.BooleanDimAttribute;
import net.minecraft.util.Identifier;

import static net.gudenau.minecraft.dims.Dims.MOD_ID;

/**
 * The backing implementation to the boolean attribute interface.
 *
 * @since 0.0.1
 */
public final class BooleanDimAttributeImpl implements BooleanDimAttribute{
    private final boolean value;
    private final Identifier id;
    
    public BooleanDimAttributeImpl(boolean value){
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
