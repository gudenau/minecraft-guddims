package net.gudenau.minecraft.dims.api.v0.controller;

import net.gudenau.minecraft.dims.api.v0.attribute.*;

/**
 * A skylight controller. Allows the user to chose if they want skylight in dimensions or not.
 *
 * @since 0.0.3
 */
public interface SkylightDimController extends DimController<SkylightDimAttribute>{
    @Override
    default ControllerType getType(){
        return ControllerType.SKYLIGHT;
    }
    
    @Override
    default boolean isPropertyValid(DimAttribute attribute){
        return attribute.getType() == DimAttributeType.BOOLEAN;
    }
}
