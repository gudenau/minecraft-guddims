package net.gudenau.minecraft.dims.accessor;

import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WorldBorder.Properties.class)
public interface WorldBorder$PropertiesAccessor{
    @SuppressWarnings("ConstantConditions")
    @Invoker("<init>") static WorldBorder.Properties init(double centerX, double centerZ, double damagePerBlock, double safeZone, int warningBlocks, int warningTime, double size, long sizeLerpTime, double sizeLerpTarget){return (WorldBorder.Properties)new Object();}
}
