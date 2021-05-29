package net.gudenau.minecraft.dims.impl.controller.biome;

import java.util.List;
import net.gudenau.minecraft.dims.api.v0.attribute.BiomeDimAttribute;
import net.gudenau.minecraft.dims.api.v0.attribute.DimAttribute;
import net.gudenau.minecraft.dims.api.v0.controller.BiomeDimController;
import net.gudenau.minecraft.dims.api.v0.util.IntRange;
import net.gudenau.minecraft.dims.api.v0.util.collection.ObjectIntPair;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.FixedBiomeSource;

import static net.gudenau.minecraft.dims.Dims.MOD_ID;

/**
 * The implementation of the single biome controller.
 *
 * @since 0.0.3
 */
public final class SingleBiomeDimControllerImpl implements BiomeDimController{
    private static final IntRange VALID_COUNT = IntRange.of(1);
    private static final Identifier ID = new Identifier(MOD_ID, "single");
    
    @Override
    public IntRange getValidBiomeCount(){
        return VALID_COUNT;
    }
    
    @Override
    public ObjectIntPair<BiomeSource> createBiomeSource(List<Biome> biomes){
        return ObjectIntPair.of(new FixedBiomeSource(biomes.get(0)), 0);
    }
    
    @Override
    public Identifier getId(){
        return ID;
    }
}
