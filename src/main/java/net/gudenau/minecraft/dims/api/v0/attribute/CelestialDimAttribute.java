package net.gudenau.minecraft.dims.api.v0.attribute;

import net.gudenau.minecraft.dims.api.v0.controller.CelestialDimController;

/**
 * An attribute that controls celestial objects.
 *
 * @since 0.0.4
 */
public interface CelestialDimAttribute extends ControllerDimAttribute<CelestialDimAttribute, CelestialDimController>{
    @Override
    default DimAttributeType getType(){
        return DimAttributeType.CELESTIAL;
    }
}
