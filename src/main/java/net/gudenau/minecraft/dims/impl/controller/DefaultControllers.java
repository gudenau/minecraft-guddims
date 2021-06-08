package net.gudenau.minecraft.dims.impl.controller;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.gudenau.minecraft.dims.api.v0.controller.*;
import net.gudenau.minecraft.dims.impl.controller.biome.CheckerboardBiomeDimControllerImpl;
import net.gudenau.minecraft.dims.impl.controller.biome.SingleBiomeDimControllerImpl;
import net.gudenau.minecraft.dims.impl.controller.celestial.controller.EndSkyController;
import net.gudenau.minecraft.dims.impl.controller.celestial.controller.MoonController;
import net.gudenau.minecraft.dims.impl.controller.celestial.controller.SunController;
import net.gudenau.minecraft.dims.impl.controller.weather.BasicWeatherController;
import net.gudenau.minecraft.dims.impl.controller.weather.VanillaWeatherController;
import net.gudenau.minecraft.dims.impl.weather.ClearWeather;
import net.gudenau.minecraft.dims.impl.weather.RainWeather;
import net.gudenau.minecraft.dims.impl.weather.ThunderWeather;

/**
 * ALl of the default controllers are created here, just a utility class for organization.
 *
 * @since 0.0.3
 */
public final class DefaultControllers{
    private DefaultControllers(){}
    
    /**
     * Create a set of all of the controllers.
     *
     * @return The set of all controllers
     */
    public static Set<DimController<?>> createControllers(){
        return Stream.of(
            createBiomeControllers(),
            createWeatherControllers(),
            createCelestialControllers()
        ).flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet());
    }
    
    /**
     * Creates a set of all biome controllers.
     *
     * @return A set of all biome controllers
     */
    private static Set<BiomeDimController> createBiomeControllers(){
        return Set.of(
            new SingleBiomeDimControllerImpl(),
            new CheckerboardBiomeDimControllerImpl()
        );
    }
    
    /**
     * Creates a set of all weather controllers.
     *
     * @return A set of all weather controllers
     */
    private static Set<WeatherDimController> createWeatherControllers(){
        return Set.of(
            new BasicWeatherController(ClearWeather.INSTANCE),
            new BasicWeatherController(RainWeather.INSTANCE),
            new BasicWeatherController(ThunderWeather.INSTANCE),
            new VanillaWeatherController("vanilla", 1),
            new VanillaWeatherController("accelerated", 2)
        );
    }
    
    /**
     * Creates a set of all celestial object controllers.
     *
     * @return A set of all celestial object controllers
     *
     * @since 0.0.4
     */
    private static Set<CelestialDimController> createCelestialControllers(){
        return Set.of(
            new SunController(),
            new MoonController(),
            new EndSkyController()
        );
    }
}
