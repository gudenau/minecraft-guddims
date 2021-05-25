package net.gudenau.minecraft.dims.api.v0.attribute;

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
    ControllerType getController();
    
    @Override
    default DimAttributeType getType(){
        return DimAttributeType.BIOME_CONTROLLER;
    }
    
    /**
     * The types of biome controllers.
     *
     * TODO, make this extendable before release
     *
     * @since 0.0.1
     */
    enum ControllerType{
        /**
         * A single biome
         */
        SINGLE("single", IntRange.of(1)),
        /**
         * Two biomes that alternate every chunk.
         */
        CHECKERBOARD("checkerboard", IntRange.of(2));
        
        private final Identifier id;
        private final IntRange biomeCountRange;
    
        ControllerType(String name, IntRange biomeCountRange){
            id = new Identifier(MOD_ID, name);
            this.biomeCountRange = biomeCountRange;
        }
    
        public Identifier getId(){
            return id;
        }
    
        /**
         * Gets the range of acceptable biome types for this controller.
         *
         * TODO examples of ranges
         *
         * @return The range of values
         */
        public IntRange getBiomeCountRange(){
            return biomeCountRange;
        }
    }
}
