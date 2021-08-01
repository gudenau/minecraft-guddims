package net.gudenau.minecraft.dims.impl.attribute;

import java.util.function.Supplier;
import net.gudenau.minecraft.dims.api.v0.attribute.FeatureDimAttribute;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.ConfiguredFeature;

public class FeatureDimAttributeImpl implements FeatureDimAttribute{
    private final Supplier<ConfiguredFeature<?, ?>> feature;
    private final int featureStep;
    private final Identifier id;
    
    public FeatureDimAttributeImpl(ConfiguredFeature<?, ?> feature, int featureStep, Identifier id){
        this.feature = ()->feature;
        this.featureStep = featureStep;
        this.id = id;
    }
    
    @Override
    public Identifier getId(){
        return id;
    }
    
    @Override
    public Supplier<ConfiguredFeature<?, ?>> getFeature(){
        return feature;
    }
    
    @Override
    public int getFeatureStep(){
        return featureStep;
    }
}
