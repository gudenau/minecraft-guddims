package net.gudenau.minecraft.dims.accessor;

import java.util.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(World.class)
public interface WorldAccessor{
    @Mutable @Accessor void setRandom(Random random);
}
