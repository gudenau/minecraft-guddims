package net.gudenau.minecraft.dims.impl.client.renderer.celestial;

import com.mojang.blaze3d.systems.RenderSystem;
import net.gudenau.minecraft.dims.accessor.client.WorldRendererAccessor;
import net.gudenau.minecraft.dims.api.v0.client.renderer.CelestialObjectRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Matrix4f;

/**
 * Responsible for rendering stars for custom dimensions.
 *
 * @since 0.0.4
 */
public final class StarsRenderer implements CelestialObjectRenderer{
    private final int red;
    private final int green;
    private final int blue;
    
    public StarsRenderer(int color){
        red = (color >> 16) & 0xFF;
        green = (color >> 8) & 0xFF;
        blue = color & 0xFF;
    }
    
    @Override
    public void render(MatrixStack matrixStack, BufferBuilder bufferBuilder, long time, float tickDelta, ClientWorld world, WorldRenderer renderer, Matrix4f projection){
        RenderSystem.disableTexture();
        float starColor = world.method_23787(tickDelta) * (1.0F - world.getRainGradient(tickDelta));
        if(starColor > 0){
            RenderSystem.setShaderColor(starColor * red, starColor * green, starColor * blue, starColor);
            ((WorldRendererAccessor)renderer).getStarsBuffer().setShader(matrixStack.peek().getPositionMatrix(), projection, GameRenderer.getPositionShader());
        }
    }
    
    @Override
    public Layer getLayer(){
        return Layer.STAR;
    }
}
