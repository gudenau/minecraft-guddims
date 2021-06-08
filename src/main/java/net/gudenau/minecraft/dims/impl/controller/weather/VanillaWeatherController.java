package net.gudenau.minecraft.dims.impl.controller.weather;

import java.util.Random;
import net.gudenau.minecraft.dims.api.v0.controller.WeatherDimController;
import net.gudenau.minecraft.dims.impl.weather.VanillaWeather;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import static net.gudenau.minecraft.dims.Dims.MOD_ID;

/**
 * The default Vanilla weather controller implementation.
 *
 * @since 0.0.3
 */
public final class VanillaWeatherController implements WeatherDimController{
    private final Identifier id;
    private final int scale;
    
    public VanillaWeatherController(String name, int scale){
        id = new Identifier(MOD_ID, name);
        this.scale = scale;
    }
    
    @Override
    public Identifier getId(){
        return id;
    }
    
    @Override
    public WeatherController createController(Random random){
        return new VanillaWeather(random, scale);
    }
    
    @Override
    public WeatherController loadController(Random random, NbtCompound tag){
        var controller = new VanillaWeather(random, scale);
        controller.fromNbt(tag);
        return controller;
    }
}
