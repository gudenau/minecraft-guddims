package net.gudenau.minecraft.dims.impl.controller.weather;

import java.util.Random;
import net.gudenau.minecraft.dims.api.v0.controller.WeatherDimController;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import static net.gudenau.minecraft.dims.Dims.MOD_ID;

/**
 * Used to hold the immutable weather controllers, clear, rain and thunder.
 *
 * @since 0.0.3
 */
public class BasicWeatherController implements WeatherDimController{
    private final WeatherController controller;
    
    public BasicWeatherController(WeatherController controller){
        this.controller = controller;
    }
    
    @Override
    public Identifier getId(){
        return controller.getId();
    }
    
    @Override
    public WeatherController createController(Random random){
        return controller;
    }
    
    @Override
    public WeatherController loadController(Random random, NbtCompound tag){
        return controller;
    }
}
