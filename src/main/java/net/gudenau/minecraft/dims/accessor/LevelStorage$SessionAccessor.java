package net.gudenau.minecraft.dims.accessor;

import java.nio.file.Path;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.SessionLock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelStorage.Session.class)
public interface LevelStorage$SessionAccessor{
    @Accessor Path getDirectory();

    @Mutable @Accessor void setDirectoryName(String value);
    @Mutable @Accessor void setDirectory(Path value);
    @Mutable @Accessor void setLock(SessionLock value);
}
