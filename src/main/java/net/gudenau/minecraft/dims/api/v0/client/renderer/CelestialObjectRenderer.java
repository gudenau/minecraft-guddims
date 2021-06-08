package net.gudenau.minecraft.dims.api.v0.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Matrix4f;

/**
 * The interface for classes responsible for rendering celestial objects.
 *
 * @since 0.0.4
 */
@Environment(EnvType.CLIENT)
public interface CelestialObjectRenderer{
    /**
     * Render a celestial object.
     *
     * @param matrixStack The matrix stack
     * @param bufferBuilder The buffer builder
     * @param time World time
     * @param tickDelta Tick delta
     * @param world The world
     * @param renderer The world renderer
     * @param projection The projection matrix
     */
    void render(MatrixStack matrixStack, BufferBuilder bufferBuilder, long time, float tickDelta, ClientWorld world, WorldRenderer renderer, Matrix4f projection);
    
    /**
     * Gets the layer of this celestial object, helps to prevent rendering issues with multiple objects.
     *
     * @return The render layer
     */
    default Layer getLayer(){
        return Layer.PLANET;
    }
    
    /**
     * The layer that this celestial object belongs to.
     *
     * @since 0.0.4
     */
    enum Layer{
        /**
         * The layer for special sky boxes, the End Sky uses this layer.
         */
        SKY,
        /**
         * The layer for stars and other global elements outside of the sky box.
         */
        STAR,
        /**
         * Everything else, like the sun and moon.
         */
        PLANET
    }
}
