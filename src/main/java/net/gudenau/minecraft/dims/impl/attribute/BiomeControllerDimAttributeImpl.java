package net.gudenau.minecraft.dims.impl.attribute;

import net.gudenau.minecraft.dims.api.v0.attribute.BiomeControllerDimAttribute;
import net.gudenau.minecraft.dims.api.v0.attribute.DimAttribute;
import net.gudenau.minecraft.dims.api.v0.attribute.DimAttributeType;
import net.minecraft.util.Identifier;

public final class BiomeControllerDimAttributeImpl implements BiomeControllerDimAttribute{
    private final Controller controller;
    
    public BiomeControllerDimAttributeImpl(Controller controller){
        this.controller = controller;
    }
    
    @Override
    public Controller getController(){
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
