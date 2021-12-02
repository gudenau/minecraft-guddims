package net.gudenau.minecraft.dims.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.gudenau.minecraft.dims.impl.client.SkyRegistry;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(DimensionEffects.class)
public abstract class DimensionEffectsMixin{
    @Inject(
        method = "byDimensionType",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void byDimensionType(DimensionType dimensionType, CallbackInfoReturnable<DimensionEffects> cir){
        SkyRegistry.getInstance().getSkyProperties(dimensionType).ifPresent(cir::setReturnValue);
    }
}
