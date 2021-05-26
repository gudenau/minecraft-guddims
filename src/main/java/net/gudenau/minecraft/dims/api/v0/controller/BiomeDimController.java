package net.gudenau.minecraft.dims.api.v0.controller;

import java.util.*;
import net.gudenau.minecraft.dims.api.v0.DimRegistry;
import net.gudenau.minecraft.dims.api.v0.attribute.BiomeDimAttribute;
import net.gudenau.minecraft.dims.api.v0.attribute.DimAttributeType;
import net.gudenau.minecraft.dims.api.v0.util.IntRange;
import net.gudenau.minecraft.dims.api.v0.util.collection.ObjectIntPair;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;

/**
 * The interface for biome controllers. This is what determines how biomes will be generated in a dimension.
 *
 * @since 0.0.3
 */
public interface BiomeDimController extends DimController<BiomeDimAttribute>{
    @Override
    default ControllerType getType(){
        return ControllerType.BIOME;
    }
    
    /**
     * Gets the valid amount of biomes for this controller.
     *
     * @return Valid biome count
     */
    IntRange getValidBiomeCount();
    
    /**
     * Used to generate a list of valid biomes for this controller, used when a player doesn't provide any biomes with
     * this controller or when this controller was randomly picked when the user didn't provide one.
     *
     * @return The list of biomes
     */
    default List<Biome> generateBiomeList(Random random){
        var range = getValidBiomeCount();
        var rangeSize = range.size();
        var biomeCount = random.nextInt(rangeSize) + range.lower();
        assert range.isValid(biomeCount) : String.format(
            "Generated biome count was invalid, range %d-%d, got %d",
            range.lower(), range.upper(), biomeCount
        );
        var biomes = new ArrayList<Biome>();
        var registry = DimRegistry.getInstance();
        for(int i = 0; i < biomeCount; i++){
            biomes.add(registry.<BiomeDimAttribute>getRandomAttribute(DimAttributeType.BIOME).getBiome());
        }
        return Collections.unmodifiableList(biomes);
    }
    
    /**
     * Creates the biome source from this controller and provided biomes.
     *
     * The list of biomes is guaranteed to be within the valid range of the biomes.
     *
     * @param biomes The provided biomes
     * @return A pair of values, the biome source and the amount of instability generated
     */
    ObjectIntPair<BiomeSource> createBiomeSource(List<Biome> biomes);
}
