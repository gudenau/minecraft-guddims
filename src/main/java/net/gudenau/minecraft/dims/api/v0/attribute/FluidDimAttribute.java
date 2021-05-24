package net.gudenau.minecraft.dims.api.v0.attribute;

import net.minecraft.fluid.Fluid;

public interface FluidDimAttribute extends DimAttribute{
    Fluid getFluid();
    
    @Override
    default DimAttributeType getType(){
        return DimAttributeType.FLUID;
    }
}
