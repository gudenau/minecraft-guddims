package net.gudenau.minecraft.dims.api.v0.attribute;

import net.gudenau.minecraft.dims.api.v0.controller.WeatherDimController;

/**
 * An attribute that represents how weather works in a dimension.
 *
 * @since 0.0.1
 */
public interface WeatherDimAttribute extends ControllerDimAttribute{
    /**
     * Gets the type of weather associated with this attribute.
     *
     * @return The associated weather type
     */
    WeatherDimController getController();
    
    @Override
    default DimAttributeType getType(){
        return DimAttributeType.WEATHER;
    }
}
