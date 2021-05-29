package net.gudenau.minecraft.dims.api.v0.attribute;

import net.gudenau.minecraft.dims.api.v0.controller.SkylightDimController;

/**
 * The attribute for a skylight controller.
 *
 * @since 0.0.3
 */
public interface SkylightDimAttribute extends ControllerDimAttribute<SkylightDimAttribute, SkylightDimController>{
    @Override
    default DimAttributeType getType(){
        return DimAttributeType.SKYLIGHT;
    }
}
