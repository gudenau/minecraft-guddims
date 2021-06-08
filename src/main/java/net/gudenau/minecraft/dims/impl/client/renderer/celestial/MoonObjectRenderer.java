package net.gudenau.minecraft.dims.impl.client.renderer.celestial;

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
 * Renders a moon object.
 *
 * @since 0.0.4
 */
@Environment(EnvType.CLIENT)
public final class MoonObjectRenderer implements SimpleCelestialObjectRenderer{
    private static final Identifier TEXTURE = new Identifier("textures/environment/moon_phases.png");
    
    private final int period;
    private final int offset;
    private final int inclination;
    private final float red;
    private final float green;
    private final float blue;
    
    public MoonObjectRenderer(int period, int offset, int inclination, int color){
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
        float moonAngle = (float)(d * 2.0D + e) / 3.0F;
        
        matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion((-90 + inclination) % 360));
        matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(moonAngle * 360));
        Matrix4f model = matrixStack.peek().getModel();
        
        RenderSystem.setShaderColor(red, green, blue, 1.0F - world.getRainGradient(tickDelta));
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int phase = world.getMoonPhase();
        int phaseX = phase % 4;
        int phaseY = phase / 4 % 2;
        float phaseU1 = phaseX / 4F;
        float phaseV1 = phaseY / 2F;
        float phaseU2 = (phaseX + 1) / 4F;
        float phaseV2 = (phaseY + 1) / 2F;
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(model, -20, 100, 20).texture(phaseU2, phaseV2).next();
        bufferBuilder.vertex(model, 20, 100, 20).texture(phaseU1, phaseV2).next();
        bufferBuilder.vertex(model, 20, 100, -20).texture(phaseU1, phaseV1).next();
        bufferBuilder.vertex(model, -20, 100, -20).texture(phaseU2, phaseV1).next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        RenderSystem.disableTexture();
    }
}
