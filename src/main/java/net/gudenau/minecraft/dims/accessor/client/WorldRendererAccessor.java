package net.gudenau.minecraft.dims.accessor.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldRenderer.class)
public interface WorldRendererAccessor{
    @Accessor ClientWorld getWorld();
    @Accessor MinecraftClient getClient();
    @Accessor VertexBuffer getLightSkyBuffer();
    @Accessor VertexBuffer getDarkSkyBuffer();
    @Accessor VertexBuffer getStarsBuffer();
}
