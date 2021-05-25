package net.gudenau.minecraft.dims.impl.attribute;

import net.gudenau.minecraft.dims.api.v0.attribute.FluidDimAttribute;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * The backing implementation to the fluid attribute interface.
 *
 * @since 0.0.1
 */
public final class FluidDimAttributeImpl implements FluidDimAttribute{
    private final Fluid fluid;
    private final Identifier id;
    
    public FluidDimAttributeImpl(Fluid fluid, Identifier id){
        this.fluid = fluid;
        this.id = id;
    }
    
    @Override
    public Identifier getId(){
        return id;
    }
    
    @Override
    public Fluid getFluid(){
        return fluid;
    }
    
    @Override
    public ItemStack getPreviewStack(){
        return new ItemStack(fluid.getBucketItem());
    }
}
