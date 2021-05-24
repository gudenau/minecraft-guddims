package gudDims.extensions.net.minecraft.entity.Entity;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import net.gudenau.minecraft.dims.api.v0.util.DimensionalTeleportTarget;
import net.gudenau.minecraft.dims.duck.EntityDuck;
import net.minecraft.entity.Entity;
import net.minecraft.world.TeleportTarget;

@Extension
public class EntityExtension {
  public static void setTeleportTarget(@This Entity thiz, TeleportTarget target){
    ((EntityDuck)thiz).gud_dims$setTeleportTarget(target);
  }
  
  public static void teleportToTarget(@This Entity thiz, DimensionalTeleportTarget target){
    if(thiz.world.isClient()){
      throw new RuntimeException("Entity.teleportToTarget called on client");
    }
    var destWorld = target.world();
    var pos = target.position();
    if(thiz.world.getRegistryKey().equals(destWorld)){
      thiz.teleport(pos.x, pos.y, pos.z);
      thiz.setYaw(target.yaw());
      thiz.setPitch(target.pitch());
    }else{
      var newWorld = thiz.world.getServer().getWorld(target.world());
      if(newWorld == null){
        System.err.println("Failed to teleport entity " + thiz + ": destination world was null");
        return;
      }
      setTeleportTarget(thiz, target.toTeleportTarget(thiz));
      thiz.moveToWorld(newWorld);
    }
  }
}