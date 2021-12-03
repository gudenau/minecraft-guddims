package net.gudenau.minecraft.dims.api.v0.attribute;

import java.util.function.Supplier;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PlacedFeature;

public interface FeatureDimAttribute extends DimAttribute{
    Supplier<PlacedFeature> getFeature();
    
    int getFeatureStep();
    
    @Override
    default DimAttributeType getType(){
        return DimAttributeType.FEATURE;
    }
}
