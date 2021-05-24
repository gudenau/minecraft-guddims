package net.gudenau.minecraft.dims.impl.weather;

import java.util.Random;
import net.gudenau.minecraft.dims.api.v0.attribute.WeatherDimAttribute;
import net.minecraft.nbt.NbtCompound;

public interface WeatherController{
    WeatherController CLEAR_WEATHER = new ClearWeather();
    WeatherController RAIN_WEATHER = new RainWeather();
    WeatherController THUNDER_WEATHER = new ThunderWeather();
    
    static WeatherController fromNbt(Random random, NbtCompound tag){
        return switch(tag.getString("type")){
            case "clear" -> CLEAR_WEATHER;
            case "rain" -> RAIN_WEATHER;
            case "thunder" -> THUNDER_WEATHER;
            case "", "vanilla" ->{
                var controller = new VanillaWeather(random, 1);
                controller.fromNbt1(tag);
                yield controller;
            }
            default -> throw new IllegalStateException("Unknown weather type: " + tag.getString("type"));
        };
    }
    
    void tick();
    
    NbtCompound toNbt();
    
    void fromNbt1(NbtCompound tag);
    
    int getClearTime();
    
    void setClearTime(int clearWeatherTime);
    
    int getRainTime();
    
    void setRainTime(int rainTime);
    
    boolean isRaining();
    
    void setRaining(boolean raining);
    
    void setThunderTime(int thunderTime);
    
    int getThunderTime();
    
    boolean isThundering();
    
    void setThundering(boolean thundering);
    
    static WeatherController createDefault(Random random){
        return create(random, WeatherDimAttribute.WeatherType.NORMAL);
    }
    
    static WeatherController create(Random random, WeatherDimAttribute.WeatherType type){
        return switch(type){
            case NORMAL -> new VanillaWeather(random, 1);
            case CLEAR -> CLEAR_WEATHER;
            case RAIN -> RAIN_WEATHER;
            case THUNDER -> THUNDER_WEATHER;
            case ACCELERATED -> new VanillaWeather(random, 4);
        };
    }
}
