package net.gudenau.minecraft.dims.mixin;

import net.gudenau.minecraft.dims.impl.DimRegistryImpl;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin{
    @Inject(
        method = "createWorlds",
        at = @At("RETURN")
    )
    private void createWorlds(WorldGenerationProgressListener worldGenerationProgressListener, CallbackInfo ci){
        DimRegistryImpl.INSTANCE.loadDimensions((MinecraftServer)(Object)this);
    }
}
