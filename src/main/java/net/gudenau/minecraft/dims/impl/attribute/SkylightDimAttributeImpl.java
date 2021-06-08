package net.gudenau.minecraft.dims.impl.attribute;

import net.gudenau.minecraft.dims.api.v0.attribute.SkylightDimAttribute;
import net.gudenau.minecraft.dims.api.v0.controller.SkylightDimController;
import net.minecraft.util.Identifier;

/**
 * The container of a skylight controller attribute.
 *
 * @since 0.0.3
 */
public final class SkylightDimAttributeImpl implements SkylightDimAttribute{
    /**
     * The controller instance.
     */
    private final SkylightDimController controller;
    
    public SkylightDimAttributeImpl(SkylightDimController controller){
        this.controller = controller;
    }
    
    @Override
    public Identifier getId(){
        return controller.getId();
    }
    
    @Override
    public SkylightDimController getController(){
        return controller;
    }
}
