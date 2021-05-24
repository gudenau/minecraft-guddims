package net.gudenau.minecraft.dims.mixin;

import net.gudenau.minecraft.dims.duck.EntityDuck;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityDuck{
    @Unique private TeleportTarget gud_dims$teleportTarget;
    
    @Unique
    @Override
    public void gud_dims$setTeleportTarget(TeleportTarget target){
        gud_dims$teleportTarget = target;
    }
    
    @Unique
    @Override
    public TeleportTarget gud_dims$getTeleportTarget(){
        return gud_dims$teleportTarget;
    }
    
    @Inject(
        method = "getTeleportTarget",
        at = @At("HEAD"),
        cancellable = true
    )
    private void getTeleportTarget(ServerWorld destination, CallbackInfoReturnable<TeleportTarget> cir){
        if(gud_dims$teleportTarget != null){
            cir.setReturnValue(gud_dims$teleportTarget);
        }
    }
    
    @Redirect(
        method = "moveToWorld",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/World;END:Lnet/minecraft/util/registry/RegistryKey;"
        )
    )
    private RegistryKey<World> moveToWorld$END(){
        return gud_dims$teleportTarget == null ? World.END : null;
    }
    
    @Inject(
        method = "moveToWorld",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;removeFromDimension()V"
        )
    )
    private void moveToWorld(ServerWorld destination, CallbackInfoReturnable<Entity> cir){
        gud_dims$teleportTarget = null;
    }
}
