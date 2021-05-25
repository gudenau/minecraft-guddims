package gudDims.extensions.net.minecraft.entity.Entity;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import net.gudenau.minecraft.dims.api.v0.util.DimensionalTeleportTarget;
import net.gudenau.minecraft.dims.duck.EntityDuck;
import net.minecraft.entity.Entity;
import net.minecraft.world.TeleportTarget;

@Extension
public class EntityExtension {
  /**
   * Sets the teleport target for the next entity teleport.
   *
   * @param target The location to target the entity to
   */
  public static void setTeleportTarget(@This Entity thiz, TeleportTarget target){
    ((EntityDuck)thiz).gud_dims$setTeleportTarget(target);
  }
  
  /**
   * Teleports an entity to the provided target, handling dimension transitions if required.
   *
   * @param target The location and dimension to teleport the entity to
   */
  public static void teleportToTarget(@This Entity thiz, DimensionalTeleportTarget target){
    // Make sure this is server side
    if(thiz.world.isClient()){
      throw new RuntimeException("Entity.teleportToTarget called on client");
    }
    var destWorld = target.world();
    var pos = target.position();
    // Check to see if the entity is in the same dimension as the target
    if(thiz.world.getRegistryKey().equals(destWorld)){
      // That's easy, just move them
      thiz.teleport(pos.x, pos.y, pos.z);
      thiz.setYaw(target.yaw());
      thiz.setPitch(target.pitch());
    }else{
      // Get the new world
      var newWorld = thiz.world.getServer().getWorld(target.world());
      if(newWorld == null){
        // Woops!
        throw new RuntimeException("Failed to teleport entity " + thiz + ": destination world was null");
      }
      // Set the teleport target and ask vanilla code to TP
      setTeleportTarget(thiz, target.toTeleportTarget(thiz));
      thiz.moveToWorld(newWorld);
    }
  }
}