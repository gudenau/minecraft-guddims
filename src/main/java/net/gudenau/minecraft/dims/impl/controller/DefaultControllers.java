package net.gudenau.minecraft.dims.impl.controller;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.gudenau.minecraft.dims.api.v0.controller.BiomeDimController;
import net.gudenau.minecraft.dims.api.v0.controller.DimController;
import net.gudenau.minecraft.dims.api.v0.controller.WeatherDimController;
import net.gudenau.minecraft.dims.impl.controller.biome.CheckerboardBiomeDimControllerImpl;
import net.gudenau.minecraft.dims.impl.controller.biome.SingleBiomeDimControllerImpl;
import net.gudenau.minecraft.dims.impl.controller.weather.BasicWeatherController;
import net.gudenau.minecraft.dims.impl.controller.weather.VanillaWeatherController;
import net.gudenau.minecraft.dims.impl.weather.ClearWeather;
import net.gudenau.minecraft.dims.impl.weather.RainWeather;
import net.gudenau.minecraft.dims.impl.weather.ThunderWeather;

public final class DefaultControllers{
    private DefaultControllers(){}
    
    public static Set<DimController<?>> createControllers(){
        return Stream.of(
            createBiomeControllers(),
            createWeatherControllers()
        ).flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet());
    }
    
    private static Set<BiomeDimController> createBiomeControllers(){
        return Set.of(
            new SingleBiomeDimControllerImpl(),
            new CheckerboardBiomeDimControllerImpl()
        );
    }
    
    private static Set<WeatherDimController> createWeatherControllers(){
        return Set.of(
            new BasicWeatherController(ClearWeather.INSTANCE),
            new BasicWeatherController(RainWeather.INSTANCE),
            new BasicWeatherController(ThunderWeather.INSTANCE),
            new VanillaWeatherController("vanilla", 1),
            new VanillaWeatherController("accelerated", 2)
        );
    }
}
