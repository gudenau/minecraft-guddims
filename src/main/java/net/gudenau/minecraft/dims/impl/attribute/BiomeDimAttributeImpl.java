package net.gudenau.minecraft.dims.impl.attribute;

import net.gudenau.minecraft.dims.api.v0.attribute.BiomeDimAttribute;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

/**
 * The backing implementation to the biome attribute interface.
 *
 * @since 0.0.1
 */
public final class BiomeDimAttributeImpl implements BiomeDimAttribute{
    private final Biome biome;
    private final Identifier id;
    
    public BiomeDimAttributeImpl(Biome biome, Identifier id){
        this.biome = biome;
        this.id = id;
    }
    
    @Override
    public Biome getBiome(){
        return biome;
    }
    
    @Override
    public Identifier getId(){
        return id;
    }
}
