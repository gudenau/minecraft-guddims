package net.gudenau.minecraft.dims.impl.attribute;

import net.gudenau.minecraft.dims.api.v0.attribute.CelestialDimAttribute;
import net.gudenau.minecraft.dims.api.v0.controller.CelestialDimController;
import net.minecraft.util.Identifier;

/**
 * The implementation of a celestial dimension attribute.
 *
 * @since 0.0.4
 */
public final class CelestialDimAttributeImpl implements CelestialDimAttribute{
    private final CelestialDimController controller;
    private final Identifier id;
    
    public CelestialDimAttributeImpl(CelestialDimController controller){
        this.controller = controller;
        this.id = controller.getId();
    }
    
    @Override
    public CelestialDimController getController(){
        return controller;
    }
    
    @Override
    public Identifier getId(){
        return id;
    }
}
