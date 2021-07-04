package net.gudenau.minecraft.dims.api.v0.attribute;

import net.gudenau.minecraft.dims.impl.controller.feature.FeatureController;

public interface FeatureControllerDimAttribute extends DimAttribute{
    @Override
    default DimAttributeType getType(){
        return DimAttributeType.FEATURE_CONTROLLER;
    }
    
    FeatureController getController();
}
