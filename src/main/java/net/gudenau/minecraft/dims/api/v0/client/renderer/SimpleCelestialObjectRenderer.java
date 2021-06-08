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
 * This mostly exists because I was being lazy with other code stuff
 *
 * @since 0.0.4
 */
@Environment(EnvType.CLIENT)
public interface SimpleCelestialObjectRenderer extends CelestialObjectRenderer{
    @Override
    default void render(MatrixStack matrixStack, BufferBuilder bufferBuilder, long time, float tickDelta, ClientWorld world, WorldRenderer renderer, Matrix4f projection){
        render(matrixStack, bufferBuilder, time, tickDelta, world);
    }
    
    /**
     * Render a celestial object.
     *
     * @param matrixStack The matrix stack
     * @param bufferBuilder The buffer builder
     * @param time World time
     * @param tickDelta Tick delta
     * @param world The world
     */
    void render(MatrixStack matrixStack, BufferBuilder bufferBuilder, long time, float tickDelta, ClientWorld world);
}
