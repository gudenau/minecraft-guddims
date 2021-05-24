package net.gudenau.minecraft.dims.api.v0.attribute;

import net.minecraft.util.DyeColor;

public interface ColorDimAttribute extends DimAttribute{
    DyeColor getColor();
    default int getColorValue(){
        return getColor().getColor();
    }
    
    @Override
    default DimAttributeType getType(){
        return DimAttributeType.COLOR;
    }
}
