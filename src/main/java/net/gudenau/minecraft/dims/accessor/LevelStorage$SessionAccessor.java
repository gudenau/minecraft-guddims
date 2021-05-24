package net.gudenau.minecraft.dims.accessor;

import java.nio.file.Path;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelStorage.Session.class)
public interface LevelStorage$SessionAccessor{
    @Accessor Path getDirectory();
}
