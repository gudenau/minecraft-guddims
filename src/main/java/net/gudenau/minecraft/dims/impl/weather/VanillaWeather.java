package net.gudenau.minecraft.dims.impl.weather;

import java.util.Random;
import net.gudenau.minecraft.dims.api.v0.controller.WeatherDimController;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import static net.gudenau.minecraft.dims.Dims.MOD_ID;

/**
 * The weather controller for dimensions with vanilla weather. Also provides a scale factor for the speed at which the
 * weather changes.
 *
 * @since 0.0.1
 */
public final class VanillaWeather implements WeatherDimController.WeatherController{
    private static final Identifier ID = new Identifier(MOD_ID, "vanilla");
    
    private final Random random;
    private int scale;
    
    private boolean isRaining = false;
    private boolean isThundering = false;
    
    private int clearTime = 0;
    private int rainTime = 0;
    private int thunderTime = 0;
    
    public VanillaWeather(Random random, int scale){
        this.random = random;
        this.scale = scale;
    }
    
    @Override
    public void tick(){
        var clearTime = this.clearTime;
        var thunderTime = this.thunderTime;
        var rainTime = this.rainTime;
        var isRaining = this.isRaining;
        var isThundering = this.isThundering;
        if(clearTime > 0){
            clearTime -= scale;
            thunderTime = isThundering ? 0 : 1;
            rainTime = isRaining ? 0 : 1;
            isThundering = false;
            isRaining = false;
        }else{
            if(thunderTime > 0){
                thunderTime -= scale;
                if(thunderTime <= 0){
                    isThundering = !isThundering;
                }
            }else if(isThundering){
                thunderTime = random.nextInt(12000) + 3600;
            } else {
                thunderTime = random.nextInt(168000) + 12000;
            }
        
            if(rainTime > 0){
                rainTime -= scale;
                if(rainTime <= 0){
                    isRaining = !isRaining;
                }
            }else if (isRaining){
                rainTime = random.nextInt(12000) + 12000;
            }else{
                rainTime = random.nextInt(168000) + 12000;
            }
        }
    
        this.clearTime = clearTime;
        this.rainTime = rainTime;
        this.thunderTime = thunderTime;
        this.isRaining = isRaining;
        this.isThundering = isThundering;
    }
    
    @Override
    public NbtCompound toNbt(){
        var tag = new NbtCompound();
        tag.putInt("scale", scale);
        tag.putInt("clearTime", clearTime);
        tag.putInt("rainTime", rainTime);
        tag.putInt("thunderTime", thunderTime);
        tag.putBoolean("rain", isRaining);
        tag.putBoolean("thunder", isThundering);
        return tag;
    }
    
    @Override
    public Identifier getId(){
        return ID;
    }
    
    public void fromNbt(NbtCompound tag){
        scale = tag.getInt("scale");
        clearTime = tag.getInt("clearTime");
        rainTime = tag.getInt("rainTime");
        thunderTime = tag.getInt("thunderTime");
        isRaining = tag.getBoolean("rain");
        isThundering = tag.getBoolean("thunder");
    }
    
    @Override
    public int getClearTime(){
        return clearTime;
    }
    
    @Override
    public void setClearTime(int clearWeatherTime){
        clearTime = clearWeatherTime;
    }
    
    @Override
    public int getRainTime(){
        return rainTime;
    }
    
    @Override
    public void setRainTime(int rainTime){
        this.rainTime = rainTime;
    }
    
    @Override
    public boolean isRaining(){
        return isRaining;
    }
    
    @Override
    public void setRaining(boolean raining){
        this.isRaining = raining;
    }
    
    @Override
    public void setThunderTime(int thunderTime){
        this.thunderTime = thunderTime;
    }
    
    @Override
    public int getThunderTime(){
        return thunderTime;
    }
    
    @Override
    public boolean isThundering(){
        return isThundering;
    }
    
    @Override
    public void setThundering(boolean thundering){
        isThundering = thundering;
    }
}
