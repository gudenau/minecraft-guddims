package net.gudenau.minecraft.dims.impl.client.renderer.celestial;

import com.mojang.blaze3d.systems.RenderSystem;
import net.gudenau.minecraft.dims.api.v0.client.renderer.SimpleCelestialObjectRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

/**
 * Responsible for rendering the End sky for custom dimensions.
 *
 * @since 0.0.4
 */
public final class EndSkyRenderer implements SimpleCelestialObjectRenderer{
    private static final Identifier TEXTURE = new Identifier("textures/environment/end_sky.png");
    private final int red;
    private final int green;
    private final int blue;
    
    public EndSkyRenderer(int color){
        red = (color >> 16) & 0xFF;
        green = (color >> 8) & 0xFF;
        blue = color & 0xFF;
    }
    
    @Override
    public void render(MatrixStack matrixStack, BufferBuilder bufferBuilder, long time, float tickDelta, ClientWorld world){
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
    
        for(int i = 0; i < 6; i++){
            matrixStack.push();
            
            switch(i){
                case 1 -> matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90.0F));
                case 2 -> matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(-90.0F));
                case 3 -> matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(180.0F));
                case 4 -> matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(90.0F));
                case 5 -> matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(-90.0F));
            }
            
            Matrix4f matrix4f = matrixStack.peek().getModel();
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            bufferBuilder.vertex(matrix4f, -100.0F, -100.0F, -100.0F).texture(0.0F, 0.0F).color(red, green, blue, 255).next();
            bufferBuilder.vertex(matrix4f, -100.0F, -100.0F, 100.0F).texture(0.0F, 16.0F).color(red, green, blue, 255).next();
            bufferBuilder.vertex(matrix4f, 100.0F, -100.0F, 100.0F).texture(16.0F, 16.0F).color(red, green, blue, 255).next();
            bufferBuilder.vertex(matrix4f, 100.0F, -100.0F, -100.0F).texture(16.0F, 0.0F).color(red, green, blue, 255).next();
            bufferBuilder.end();
            BufferRenderer.draw(bufferBuilder);
            matrixStack.pop();
        }
    
        RenderSystem.depthMask(true);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
    
    @Override
    public Layer getLayer(){
        return Layer.SKY;
    }
}
