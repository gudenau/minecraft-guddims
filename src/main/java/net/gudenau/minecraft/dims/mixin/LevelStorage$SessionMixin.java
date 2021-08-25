package net.gudenau.minecraft.dims.mixin;

import java.io.File;
import java.nio.file.Path;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelStorage.Session.class)
public abstract class LevelStorage$SessionMixin{
    @Shadow Path directory;
    
    @Inject(
        method = "getWorldDirectory",
        at = @At("HEAD"),
        cancellable = true
    )
    private void getWorldDirectory(RegistryKey<World> key, CallbackInfoReturnable<File> cir){
        if(key == null){
            cir.setReturnValue(directory.toFile());
        }
    }
}
