package net.gudenau.minecraft.dims.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.gudenau.minecraft.dims.api.v0.client.DimensionSkyProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin{
    @Shadow @Final private MinecraftClient client;
    
    @Shadow private ClientWorld world;
    
    @Inject(
        method = "renderSky",
        at = @At("HEAD"),
        cancellable = true
    )
    private void renderSky(MatrixStack matrixStack, Matrix4f projection, float tickDelta, Runnable runnable, CallbackInfo ci){
        var skyProps = client.world.getSkyProperties();
        if(skyProps instanceof DimensionSkyProperties dimSkyPros){
            runnable.run();
            dimSkyPros.render(matrixStack, projection, tickDelta, (WorldRenderer)(Object)this);
            ci.cancel();
        }
    }
}
