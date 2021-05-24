package net.gudenau.minecraft.dims.mixin;

import net.gudenau.minecraft.dims.duck.EntityDuck;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements EntityDuck{
    @Redirect(
        method = "moveToWorld",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/World;END:Lnet/minecraft/util/registry/RegistryKey;"
        )
    )
    private RegistryKey<World> moveToWorld$END(){
        return gud_dims$getTeleportTarget() == null ? World.END : null;
    }
    
    @Redirect(
        method = "moveToWorld",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/World;NETHER:Lnet/minecraft/util/registry/RegistryKey;"
        )
    )
    private RegistryKey<World> moveToWorld$NETHER(){
        return gud_dims$getTeleportTarget() == null ? World.NETHER : null;
    }
    
    @Inject(
        method = "moveToWorld",
        at = @At("RETURN")
    )
    private void moveToWorldTail(ServerWorld destination, CallbackInfoReturnable<Entity> cir){
        gud_dims$setTeleportTarget(null);
    }
}
