package net.gudenau.minecraft.dims.impl.client.renderer.celestial;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.gudenau.minecraft.dims.api.v0.client.renderer.SimpleCelestialObjectRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

/**
 * Renders a sun object.
 *
 * @since 0.0.4
 */
@Environment(EnvType.CLIENT)
public final class SunObjectRenderer implements SimpleCelestialObjectRenderer{
    private static final Identifier TEXTURE = new Identifier("textures/environment/sun.png");
    
    private final int period;
    private final int offset;
    private final int inclination;
    private final float red;
    private final float green;
    private final float blue;
    
    public SunObjectRenderer(int period, int offset, int inclination, int color){
        this.period = period;
        this.offset = offset;
        this.inclination = inclination;
        red = ((color >> 16) & 0xFF) * 0.003921569F;
        green = ((color >> 8) & 0xFF) * 0.003921569F;
        blue = (color & 0xFF) * 0.003921569F;
    }
    
    @Override
    public void render(MatrixStack matrixStack, BufferBuilder bufferBuilder, long time, float tickDelta, ClientWorld world){
        double d = MathHelper.fractionalPart((time + offset) / (double)period - 0.25);
        double e = 0.5D - Math.cos(d * 3.141592653589793D) / 2.0D;
        float sunAngle = (float)(d * 2.0D + e) / 3.0F;
        
        matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion((-90 + inclination) % 360));
        matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(sunAngle * 360));
        Matrix4f model = matrixStack.peek().getModel();
    
        RenderSystem.enableTexture();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        
        RenderSystem.setShaderColor(red, green, blue, 1.0F - world.getRainGradient(tickDelta));
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(model, -30, 100, -30).texture(0.0F, 0.0F).next();
        bufferBuilder.vertex(model, 30, 100, -30).texture(1.0F, 0.0F).next();
        bufferBuilder.vertex(model, 30, 100, 30).texture(1.0F, 1.0F).next();
        bufferBuilder.vertex(model, -30, 100, 30).texture(0.0F, 1.0F).next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
    }
}
