package net.gudenau.minecraft.dims.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.gudenau.minecraft.dims.client.BlockColorCache;
import net.minecraft.client.resource.ResourceReloadLogger;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ResourceReloadLogger.class)
public abstract class ResourceReloadLoggerMixin{
    @Inject(
        method = "finish",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.PUTFIELD,
            target = "Lnet/minecraft/client/resource/ResourceReloadLogger$ReloadState;finished:Z"
        )
    )
    private void finish(CallbackInfo ci){
        BlockColorCache.reload();
    }
}
