package net.gudenau.minecraft.dims.impl.attribute;

import net.gudenau.minecraft.dims.api.v0.attribute.BiomeControllerDimAttribute;
import net.gudenau.minecraft.dims.api.v0.attribute.DimAttribute;
import net.gudenau.minecraft.dims.api.v0.attribute.DimAttributeType;
import net.minecraft.util.Identifier;

/**
 * The backing implementation to the biome controller attribute interface.
 *
 * @since 0.0.1
 */
public final class BiomeControllerDimAttributeImpl implements BiomeControllerDimAttribute{
    private final ControllerType controllerType;
    
    public BiomeControllerDimAttributeImpl(ControllerType controllerType){
        this.controllerType = controllerType;
    }
    
    @Override
    public ControllerType getController(){
        return controllerType;
    }
    
    @Override
    public Identifier getId(){
        return controllerType.getId();
    }
    
    @Override
    public boolean isPropertyValid(DimAttribute attribute){
        return attribute.getType() == DimAttributeType.BIOME;
    }
}
