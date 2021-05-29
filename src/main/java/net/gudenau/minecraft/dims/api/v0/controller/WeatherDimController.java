package net.gudenau.minecraft.dims.api.v0.controller;

import java.util.List;
import java.util.Random;
import net.gudenau.minecraft.dims.api.v0.attribute.DimAttribute;
import net.gudenau.minecraft.dims.api.v0.attribute.WeatherDimAttribute;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

/**
 * A controller for weather. Used to control how weather works in dimensions.
 *
 * @since 0.0.3
 */
public interface WeatherDimController extends DimController<WeatherDimAttribute>{
    /**
     * Used to create a new weather controller.
     *
     * @param random The world's random instance
     * @param attributes The user provided attributes
     * @return The new weather controller
     */
    default WeatherController createController(Random random, List<DimAttribute> attributes){
        return createController(random);
    }
    
    /**
     * Used to create a new weather controller.
     *
     * @param random The world's random instance
     * @return The new weather controller
     */
    WeatherController createController(Random random);
    
    /**
     * Used to load a saved weather controller.
     *
     * @param random The world's random instance
     * @param tag The tag created with {@link WeatherController#toNbt}
     * @return The loaded weather controller
     */
    WeatherController loadController(Random random, NbtCompound tag);
    
    @Override
    default ControllerType getType(){
        return ControllerType.WEATHER;
    }
    
    @Override
    default boolean isPropertyValid(DimAttribute attribute){
        return false;
    }
    
    /**
     * Vanilla doesn't abstract weather this way, we need an extra layer of abstraction.
     *
     * Basically just delegated from our implementation of {@link net.minecraft.world.level.ServerWorldProperties}.
     *
     * @since 0.0.3
     */
    interface WeatherController{
        /**
         * Called every time the world ticks, used to update the weather.
         */
        void tick();
    
        /**
         * Used to serialize this controller.
         *
         * @return The serialized tag
         */
        NbtCompound toNbt();
    
        /**
         * Gets the identifier of the {@link WeatherDimController}
         *
         * This must match or serialization will not work.
         *
         * @return The id of the {@link WeatherDimController}
         */
        Identifier getId();
        
        int getClearTime();
    
        void setClearTime(int clearWeatherTime);
    
        int getRainTime();
    
        void setRainTime(int rainTime);
    
        boolean isRaining();
    
        void setRaining(boolean raining);
    
        int getThunderTime();
    
        void setThunderTime(int thunderTime);
    
        boolean isThundering();
    
        void setThundering(boolean thundering);
    }
}
