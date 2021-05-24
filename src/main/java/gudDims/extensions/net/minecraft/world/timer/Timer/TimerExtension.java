package gudDims.extensions.net.minecraft.world.timer.Timer;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import net.gudenau.minecraft.dims.accessor.TimerAccessor;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.timer.Timer;
import net.minecraft.world.timer.TimerCallbackSerializer;

@Extension
public class TimerExtension {
  @Extension
  public static <T> Timer<T> fromNbt(NbtList list, TimerCallbackSerializer<T> serializer) {
    var timer = new Timer<>(serializer);
    @SuppressWarnings("ConstantConditions")
    var timerAccessor = (TimerAccessor)timer;
    list.stream()
        .map((tag)->(NbtCompound)tag)
        .forEach(timerAccessor::invokeAddEvent);
    return timer;
  }
}