package net.gudenau.minecraft.dims.api.v0.attribute;

import net.gudenau.minecraft.dims.api.v0.util.IntRange;
import net.minecraft.util.Identifier;

import static net.gudenau.minecraft.dims.Dims.MOD_ID;

public interface BiomeControllerDimAttribute extends ControllerDimAttribute{
    Controller getController();
    
    @Override
    default DimAttributeType getType(){
        return DimAttributeType.BIOME_CONTROLLER;
    }
    
    enum Controller{
        SINGLE("single", IntRange.of(1)),
        CHECKERBOARD("checkerboard", IntRange.of(2));
        
        private final Identifier id;
        private final IntRange biomeCountRange;
    
        Controller(String name, IntRange biomeCountRange){
            id = new Identifier(MOD_ID, name);
            this.biomeCountRange = biomeCountRange;
        }
    
        public Identifier getId(){
            return id;
        }
    
        public IntRange getBiomeCountRange(){
            return biomeCountRange;
        }
    }
}
