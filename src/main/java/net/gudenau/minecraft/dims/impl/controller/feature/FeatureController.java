package net.gudenau.minecraft.dims.impl.controller.feature;

import net.gudenau.minecraft.dims.api.v0.attribute.DimAttribute;
import net.gudenau.minecraft.dims.api.v0.attribute.DimAttributeType;
import net.gudenau.minecraft.dims.api.v0.controller.FeatureDimController;
import net.minecraft.util.Identifier;

import static net.gudenau.minecraft.dims.Dims.MOD_ID;

/**
 * The default feature controller for manual configuration of features by the user.
 *
 * @since 0.0.6
 * */
public class FeatureController implements FeatureDimController{
    private static final Identifier ID = new Identifier(MOD_ID, "feature_controller");
    
    @Override
    public boolean isPropertyValid(DimAttribute attribute){
        var type = attribute.getType();
        return type == DimAttributeType.BIOME || type == DimAttributeType.FEATURE;
    }
    
    @Override
    public Identifier getId(){
        return ID;
    }
}
