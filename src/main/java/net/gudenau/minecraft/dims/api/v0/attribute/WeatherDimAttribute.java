package net.gudenau.minecraft.dims.api.v0.attribute;

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
    WeatherType getWeather();
    
    @Override
    default DimAttributeType getType(){
        return DimAttributeType.WEATHER;
    }
    
    //TODO Make this extendable
    enum WeatherType{
        NORMAL, CLEAR, RAIN, THUNDER, ACCELERATED
    }
}
