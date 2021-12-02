package net.gudenau.minecraft.dims.impl.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.*;
import net.gudenau.minecraft.dims.accessor.client.WorldRendererAccessor;
import net.gudenau.minecraft.dims.api.v0.client.CustomDimensionEffects;
import net.gudenau.minecraft.dims.api.v0.client.renderer.CelestialObjectRenderer;
import net.gudenau.minecraft.dims.api.v0.controller.CelestialDimController;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.*;

/**
 * Responsible for how the sky looks in custom dimensions.
 *
 * @since 0.0.4
 */
public final class CustomDimensionEffectsImpl extends DimensionEffects implements CustomDimensionEffects{
    /**
     * These render things like the sun and moon.
     */
    private final List<CelestialObjectRenderer> celestialRenderers;
    
    public CustomDimensionEffectsImpl(List<CelestialDimController.CelestialObject> objects){
        //TODO Add these as attributes
        super(128, true, DimensionEffects.SkyType.NORMAL, false, false);
        
        var skyRenderers = new ArrayList<CelestialObjectRenderer>();
        var starRenderers = new ArrayList<CelestialObjectRenderer>();
        var planetRenderers = new ArrayList<CelestialObjectRenderer>();
        
        for(CelestialDimController.CelestialObject object : objects){
            var renderer = object.createRenderer();
            (switch(renderer.getLayer()){
                case SKY -> skyRenderers;
                case STAR -> starRenderers;
                case PLANET -> planetRenderers;
            }).add(renderer);
        }
        
        var renderers = new ArrayList<>(skyRenderers);
        renderers.addAll(skyRenderers);
        renderers.addAll(planetRenderers);
        celestialRenderers = Collections.unmodifiableList(renderers);
    }
    
    @Override
    public Vec3d adjustFogColor(Vec3d color, float sunHeight){
        return color;
    }
    
    @Override
    public boolean useThickFog(int camX, int camY){
        return false;
    }
    
    @Override
    public void render(MatrixStack matrixStack, Matrix4f projection, float tickDelta, WorldRenderer worldRenderer){
        var worldRendererAccessor = (WorldRendererAccessor)worldRenderer;
        var world = worldRendererAccessor.getWorld();
        var client = worldRendererAccessor.getClient();
        
        RenderSystem.disableTexture();
        Vec3d vec3d = world.getSkyColor(client.gameRenderer.getCamera().getPos(), tickDelta);
        float g = (float)vec3d.x;
        float h = (float)vec3d.y;
        float i = (float)vec3d.z;
        BackgroundRenderer.setFogBlack();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(g, h, i, 1.0F);
        Shader shader = RenderSystem.getShader();
        worldRendererAccessor.getLightSkyBuffer().setShader(matrixStack.peek().getPositionMatrix(), projection, shader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        float[] fogColorOverride = world.getDimensionEffects().getFogColorOverride(world.getSkyAngle(tickDelta), tickDelta);
        if(fogColorOverride != null){
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.disableTexture();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            matrixStack.push();
            matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90.0F));
            float skyAngle = MathHelper.sin(world.getSkyAngleRadians(tickDelta)) < 0.0F ? 180.0F : 0.0F;
            matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(skyAngle));
            matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(90.0F));
            float fogRed = fogColorOverride[0];
            float fogGreen = fogColorOverride[1];
            float fogBlue = fogColorOverride[2];
            float fogAlpha = fogColorOverride[3];
            Matrix4f matrix4f2 = matrixStack.peek().getPositionMatrix();
            bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(matrix4f2, 0.0F, 100.0F, 0.0F).color(fogRed, fogGreen, fogBlue, fogAlpha).next();
        
            for(int o = 0; o <= 16; o++){
                float p = o * 6.2831855F / 16.0F;
                float q = MathHelper.sin(p);
                float r = MathHelper.cos(p);
                bufferBuilder.vertex(matrix4f2, q * 120.0F, r * 120.0F, -r * 40.0F * fogAlpha).color(fogRed, fogGreen, fogBlue, 0.0F).next();
            }
        
            bufferBuilder.end();
            BufferRenderer.draw(bufferBuilder);
            matrixStack.pop();
        }
    
        RenderSystem.enableTexture();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        matrixStack.push();
        long time = world.getLunarTime();
        for(var renderer : celestialRenderers){
            matrixStack.push();
            renderer.render(matrixStack, bufferBuilder, time, tickDelta, world, worldRenderer, projection);
            matrixStack.pop();
        }
    
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        matrixStack.pop();
        
        RenderSystem.disableTexture();
        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
        double darknessHeight = client.player.getCameraPosVec(tickDelta).y - world.getLevelProperties().getSkyDarknessHeight(world);
        if(darknessHeight < 0.0D){
            matrixStack.push();
            matrixStack.translate(0.0D, 12.0D, 0.0D);
            worldRendererAccessor.getDarkSkyBuffer().setShader(matrixStack.peek().getPositionMatrix(), projection, shader);
            matrixStack.pop();
        }
    
        if(world.getDimensionEffects().isAlternateSkyColor()){
            RenderSystem.setShaderColor(g * 0.2F + 0.04F, h * 0.2F + 0.04F, i * 0.6F + 0.1F, 1.0F);
        }else{
            RenderSystem.setShaderColor(g, h, i, 1.0F);
        }
    
        RenderSystem.enableTexture();
        RenderSystem.depthMask(true);
    }
}
