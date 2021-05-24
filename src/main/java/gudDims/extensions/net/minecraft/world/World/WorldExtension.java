package gudDims.extensions.net.minecraft.world.World;

import java.util.function.Consumer;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

@Extension
public class WorldExtension {
  public static void forEachPlayer(@This World thiz, Consumer<PlayerEntity> action) {
    thiz.getPlayers().forEach(action);
  }
}