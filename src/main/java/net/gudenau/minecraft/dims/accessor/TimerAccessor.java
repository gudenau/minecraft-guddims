package net.gudenau.minecraft.dims.accessor;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.timer.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Timer.class)
public interface TimerAccessor{
    @Invoker void invokeAddEvent(NbtCompound nbt);
}
