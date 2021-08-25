package net.gudenau.minecraft.dims.mixin;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static net.gudenau.minecraft.dims.Dims.MOD_ID;

@Mixin(targets = "net.minecraft.server.world.ServerChunkManager$MainThreadExecutor")
public abstract class ServerChunkManager$MainThreadExecutorMixin{
    @Unique private static final Identifier gud_dims$DUMMY_IDENTIFIER = new Identifier(MOD_ID, "dummy_world");
    
    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/registry/RegistryKey;getValue()Lnet/minecraft/util/Identifier;"
        )
    )
    private static Identifier getRegistryName(RegistryKey<World> registryKey){
        return registryKey == null ? gud_dims$DUMMY_IDENTIFIER : registryKey.getValue();
    }
}
