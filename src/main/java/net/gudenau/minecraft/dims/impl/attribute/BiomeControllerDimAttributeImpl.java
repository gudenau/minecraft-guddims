package net.gudenau.minecraft.dims.impl.attribute;

import net.gudenau.minecraft.dims.api.v0.attribute.BiomeControllerDimAttribute;
import net.gudenau.minecraft.dims.api.v0.attribute.DimAttribute;
import net.gudenau.minecraft.dims.api.v0.attribute.DimAttributeType;
import net.gudenau.minecraft.dims.api.v0.controller.BiomeDimController;
import net.minecraft.util.Identifier;

/**
 * The backing implementation to the biome controller attribute interface.
 *
 * @since 0.0.1
 */
public final class BiomeControllerDimAttributeImpl implements BiomeControllerDimAttribute{
    private final BiomeDimController controller;
    
    public BiomeControllerDimAttributeImpl(BiomeDimController controller){
        this.controller = controller;
    }
    
    @Override
    public BiomeDimController getController(){
        return controller;
    }
    
    @Override
    public Identifier getId(){
        return controller.getId();
    }
    
    @Override
    public boolean isPropertyValid(DimAttribute attribute){
        return attribute.getType() == DimAttributeType.BIOME;
    }
}
