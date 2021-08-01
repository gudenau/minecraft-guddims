package net.gudenau.minecraft.dims.api.v0.controller;

import net.gudenau.minecraft.dims.api.v0.attribute.DimAttribute;
import net.gudenau.minecraft.dims.api.v0.attribute.DimAttributeType;
import net.gudenau.minecraft.dims.api.v0.attribute.FeatureControllerDimAttribute;

public interface FeatureDimController extends DimController<FeatureControllerDimAttribute>{
    @Override
    default boolean isPropertyValid(DimAttribute attribute){
        var type = attribute.getType();
        return type == DimAttributeType.FEATURE || type == DimAttributeType.BOOLEAN;
    }
    
    @Override
    default ControllerType getType(){
        return ControllerType.FEATURE;
    }
    
    @Override
    default boolean areDuplicatesAllowed(){
        return true;
    }
}
