package net.gudenau.minecraft.dims.api.v0.attribute;

import net.minecraft.world.gen.feature.Feature;

public interface FeatureDimAttribute extends DimAttribute{
    Feature<?> getFeature();
    
    @Override
    default DimAttributeType getType(){
        return DimAttributeType.FEATURE;
    }
}
