package net.gudenau.minecraft.dims.impl.controller.biome;

import com.mojang.serialization.Codec;
import java.util.List;
import net.gudenau.minecraft.dims.api.v0.controller.BiomeDimController;
import net.gudenau.minecraft.dims.api.v0.util.IntRange;
import net.gudenau.minecraft.dims.api.v0.util.collection.ObjectIntPair;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.FixedBiomeSource;

import static net.gudenau.minecraft.dims.Dims.MOD_ID;

/**
 * The implementation of the checkerboard biome controller.
 *
 * @since 0.0.3
 */
public final class CheckerboardBiomeDimControllerImpl implements BiomeDimController{
    private static final IntRange VALID_COUNT = IntRange.of(2);
    private static final Identifier ID = new Identifier(MOD_ID, "checkerboard");
    
    @Override
    public IntRange getValidBiomeCount(){
        return VALID_COUNT;
    }
    
    @Override
    public ObjectIntPair<BiomeSource> createBiomeSource(List<Biome> biomes){
        return ObjectIntPair.of(new CheckerboardBiomeSource(biomes.get(0), biomes.get(1)), 0);
    }
    
    @Override
    public Identifier getId(){
        return ID;
    }
    
    /**
     * The vanilla version of this leaves things to be desired.
     *
     * @since 0.0.3
     */
    private static class CheckerboardBiomeSource extends BiomeSource{
        private final Biome biomeA;
        private final Biome biomeB;
        
        protected CheckerboardBiomeSource(Biome biomeA, Biome biomeB){
            super(List.of(biomeA, biomeB));
            this.biomeA = biomeA;
            this.biomeB = biomeB;
        }
        
        @Override
        protected Codec<? extends BiomeSource> getCodec(){
            return null;
        }
        
        @Override
        public BiomeSource withSeed(long seed){
            return this;
        }
        
        @Override
        public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ){
            return (((biomeX >> 4) & 1) ^ ((biomeZ >> 4) & 1)) == 0 ? biomeA : biomeB;
        }
        
        @Override
        public Biome getBiomeForNoiseGen(ChunkPos chunkPos){
            return ((chunkPos.x & 1) ^ (chunkPos.z & 1)) == 0 ? biomeA : biomeB;
        }
    }
}
