package net.gudenau.minecraft.dims.api.v0.attribute;

import net.minecraft.fluid.Fluid;

/**
 * An attribute that represents a fluid.
 *
 * @since 0.0.1
 */
public interface FluidDimAttribute extends DimAttribute{
    /**
     * Gets the fluid that is associated with this attribute.
     *
     * @return The associated fluid
     */
    Fluid getFluid();
    
    @Override
    default DimAttributeType getType(){
        return DimAttributeType.FLUID;
    }
}
