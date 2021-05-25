package net.gudenau.minecraft.dims.impl.weather;

import net.minecraft.nbt.NbtCompound;

/**
 * The weather controller for dimensions that are always clear.
 *
 * @since 0.0.1
 */
public final class ClearWeather implements WeatherController{
    @Override
    public void tick(){}
    
    @Override
    public NbtCompound toNbt(){
        var tag = new NbtCompound();
        tag.putString("type", "clear");
        return tag;
    }
    
    @Override
    public void fromNbt1(NbtCompound tag){}
    
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
