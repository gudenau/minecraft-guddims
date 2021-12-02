package net.gudenau.minecraft.dims.api.v0.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;

/**
 * Custom dimension effects for dimensions.
 *
 * @since 0.0.7
 */
@Environment(EnvType.CLIENT)
public interface CustomDimensionEffects{
    /**
     * Renders the sky of a custom dimension.
     *
     * Never used for dimensions outside of this mod.
     *
     * @param matrixStack The matrix stack
     * @param projection The projection matrix
     * @param tickDelta The tick delta
     * @param worldRenderer The world renderer
     */
    void render(MatrixStack matrixStack, Matrix4f projection, float tickDelta, WorldRenderer worldRenderer);
}
