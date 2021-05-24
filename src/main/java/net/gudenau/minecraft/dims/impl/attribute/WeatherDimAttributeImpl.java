package net.gudenau.minecraft.dims.impl.attribute;

import java.util.Locale;
import net.gudenau.minecraft.dims.api.v0.attribute.DimAttribute;
import net.gudenau.minecraft.dims.api.v0.attribute.WeatherDimAttribute;
import net.minecraft.util.Identifier;

import static net.gudenau.minecraft.dims.Dims.MOD_ID;

public final class WeatherDimAttributeImpl implements WeatherDimAttribute{
    private final WeatherType type;
    private final Identifier id;
    
    public WeatherDimAttributeImpl(WeatherType type){
        this.type = type;
        id = new Identifier(MOD_ID, type.name().toLowerCase(Locale.ROOT));
    }
    
    @Override
    public boolean isPropertyValid(DimAttribute attribute){
        return false;
    }
    
    @Override
    public Identifier getId(){
        return id;
    }
    
    @Override
    public WeatherType getWeather(){
        return type;
    }
}
