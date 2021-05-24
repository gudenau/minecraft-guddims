package net.gudenau.minecraft.dims.accessor;

import java.util.Map;
import java.util.concurrent.Executor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor{
    @Accessor LevelStorage.Session getSession();
    @Accessor Map<RegistryKey<World>, ServerWorld> getWorlds();
    @Accessor Executor getWorkerExecutor();
}
