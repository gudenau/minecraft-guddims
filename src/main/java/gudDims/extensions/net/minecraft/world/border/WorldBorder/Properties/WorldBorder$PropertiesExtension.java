package gudDims.extensions.net.minecraft.world.border.WorldBorder.Properties;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import net.gudenau.minecraft.dims.accessor.WorldBorder$PropertiesAccessor;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.border.WorldBorder.Properties;

@Extension
public class WorldBorder$PropertiesExtension {
  public static NbtCompound toNbt(@This Properties thiz) {
    var compound = new NbtCompound();
    thiz.writeNbt(compound);
    return compound;
  }
  
  @Extension
  public static WorldBorder.Properties fromNbt(NbtCompound tag){
    return WorldBorder$PropertiesAccessor.init(
        tag.getDouble("centerX"),
        tag.getDouble("centerZ"),
        tag.getDouble("damagePerBlock"),
        tag.getDouble("safeZone"),
        tag.getInt("warningBlocks"),
        tag.getInt("warningTime"),
        tag.getDouble("size"),
        tag.getLong("sizeLerpTime"),
        tag.getDouble("sizeLerpTarget")
    );
  }
}