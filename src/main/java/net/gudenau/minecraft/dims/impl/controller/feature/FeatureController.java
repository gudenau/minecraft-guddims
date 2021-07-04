package net.gudenau.minecraft.dims.impl.controller.feature;

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
    public Identifier getId(){
        return ID;
    }
}
