package net.gudenau.minecraft.dims.api.v0.attribute;

import net.minecraft.world.biome.Biome;

/**
 * An attribute that represents a single biome.
 *
 * @since 0.0.1
 */
public interface BiomeDimAttribute extends DimAttribute{
    /**
     * Gets the biome associated with this attribute.
     *
     * @return The associated biome
     */
    Biome getBiome();
    
    @Override
    default DimAttributeType getType(){
        return DimAttributeType.BIOME;
    }
}
