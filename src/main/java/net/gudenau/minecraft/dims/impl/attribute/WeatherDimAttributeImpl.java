package net.gudenau.minecraft.dims.impl.attribute;

import net.gudenau.minecraft.dims.api.v0.attribute.DimAttribute;
import net.gudenau.minecraft.dims.api.v0.attribute.WeatherDimAttribute;
import net.gudenau.minecraft.dims.api.v0.controller.WeatherDimController;
import net.minecraft.util.Identifier;

/**
 * The backing implementation to the weather attribute interface.
 *
 * @since 0.0.1
 */
public final class WeatherDimAttributeImpl implements WeatherDimAttribute{
    private final WeatherDimController controller;
    private final Identifier id;
    
    public WeatherDimAttributeImpl(WeatherDimController controller){
        this.controller = controller;
        id = controller.getId();
    }
    
    @Override
    public Identifier getId(){
        return id;
    }
    
    @Override
    public WeatherDimController getController(){
        return controller;
    }
}
