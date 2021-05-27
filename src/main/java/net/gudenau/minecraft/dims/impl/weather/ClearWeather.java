package net.gudenau.minecraft.dims.impl.weather;

import net.gudenau.minecraft.dims.api.v0.controller.WeatherDimController;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import static net.gudenau.minecraft.dims.Dims.MOD_ID;

/**
 * The weather controller for dimensions that are always clear.
 *
 * @since 0.0.1
 */
public final class ClearWeather implements WeatherDimController.WeatherController{
    public static final WeatherDimController.WeatherController INSTANCE = new ClearWeather();
    private static final Identifier ID = new Identifier(MOD_ID, "clear");
    
    private ClearWeather(){}
    
    @Override
    public void tick(){}
    
    @Override
    public NbtCompound toNbt(){
        return new NbtCompound();
    }
    
    @Override
    public Identifier getId(){
        return ID;
    }
    
    @Override
    public int getClearTime(){
        return 0;
    }
    
    @Override
    public void setClearTime(int clearWeatherTime){}
    
    @Override
    public int getRainTime(){
        return 0;
    }
    
    @Override
    public void setRainTime(int rainTime){}
    
    @Override
    public boolean isRaining(){
        return false;
    }
    
    @Override
    public void setRaining(boolean raining){}
    
    @Override
    public void setThunderTime(int thunderTime){}
    
    @Override
    public int getThunderTime(){
        return 0;
    }
    
    @Override
    public boolean isThundering(){
        return false;
    }
    
    @Override
    public void setThundering(boolean thundering){}
}
