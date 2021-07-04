package net.gudenau.minecraft.dims.impl.attribute;

import net.gudenau.minecraft.dims.api.v0.attribute.FeatureDimAttribute;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.Feature;

public class FeatureDimAttributeImpl implements FeatureDimAttribute{
    private final Feature<?> feature;
    private final Identifier id;
    
    public FeatureDimAttributeImpl(Feature<?> feature, Identifier id){
        this.feature = feature;
        this.id = id;
    }
    
    @Override
    public Identifier getId(){
        return id;
    }
    
    @Override
    public Feature<?> getFeature(){
        return feature;
    }
}
