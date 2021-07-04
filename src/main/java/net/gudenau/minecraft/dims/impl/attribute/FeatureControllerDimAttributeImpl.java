package net.gudenau.minecraft.dims.impl.attribute;

import net.gudenau.minecraft.dims.api.v0.attribute.FeatureControllerDimAttribute;
import net.gudenau.minecraft.dims.impl.controller.feature.FeatureController;
import net.minecraft.util.Identifier;

public class FeatureControllerDimAttributeImpl implements FeatureControllerDimAttribute{
    private final FeatureController controller;
    private final Identifier id;
    
    public FeatureControllerDimAttributeImpl(FeatureController controller){
        this.controller = controller;
        this.id = controller.getId();
    }
    
    @Override
    public FeatureController getController(){
        return controller;
    }
    
    @Override
    public Identifier getId(){
        return id;
    }
}
