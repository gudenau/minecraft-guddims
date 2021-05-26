package net.gudenau.minecraft.dims.api.v0.attribute;

import net.gudenau.minecraft.dims.api.v0.controller.BiomeDimController;
import net.gudenau.minecraft.dims.api.v0.util.IntRange;
import net.minecraft.util.Identifier;

import static net.gudenau.minecraft.dims.Dims.MOD_ID;

/**
 * An attribute that represents a biome controller. This is what determines how the biomes will be distributed in a
 * custom dimension.
 *
 * @since 0.0.1
 */
public interface BiomeControllerDimAttribute extends ControllerDimAttribute{
    /**
     * Gets the controller type of this attribute.
     *
     * @return The controller type
     */
    BiomeDimController getController();
    
    @Override
    default DimAttributeType getType(){
        return DimAttributeType.BIOME_CONTROLLER;
    }
}
