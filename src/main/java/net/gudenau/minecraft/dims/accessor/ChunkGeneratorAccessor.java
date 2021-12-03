package net.gudenau.minecraft.dims.accessor;

import net.minecraft.util.math.BlockBox;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@SuppressWarnings("ConstantConditions")
@Mixin(ChunkGenerator.class)
public interface ChunkGeneratorAccessor{
    @Invoker static BlockBox invokeGetBlockBoxForChunk(Chunk chunk){ return (BlockBox)new Object(); }
}
