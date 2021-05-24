package net.gudenau.minecraft.dims.api.v0.attribute;

public interface WeatherDimAttribute extends ControllerDimAttribute{
    WeatherType getWeather();
    
    @Override
    default DimAttributeType getType(){
        return DimAttributeType.WEATHER;
    }
    
    enum WeatherType{
        NORMAL, CLEAR, RAIN, THUNDER, ACCELERATED
    }
}
