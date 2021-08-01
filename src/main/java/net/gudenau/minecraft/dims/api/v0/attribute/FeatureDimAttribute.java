package net.gudenau.minecraft.dims.api.v0.attribute;

import java.util.function.Supplier;
import net.minecraft.world.gen.feature.ConfiguredFeature;

public interface FeatureDimAttribute extends DimAttribute{
    Supplier<ConfiguredFeature<?, ?>> getFeature();
    
    int getFeatureStep();
    
    @Override
    default DimAttributeType getType(){
        return DimAttributeType.FEATURE;
    }
}
