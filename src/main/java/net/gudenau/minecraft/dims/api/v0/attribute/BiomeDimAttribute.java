package net.gudenau.minecraft.dims.api.v0.attribute;

import net.minecraft.world.biome.Biome;

public interface BiomeDimAttribute extends DimAttribute{
    Biome getBiome();
    
    @Override
    default DimAttributeType getType(){
        return DimAttributeType.BIOME;
    }
}
