package net.gudenau.minecraft.dims.api.v0.util;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

public record DimensionalTeleportTarget(
    Vec3d position,
    float yaw,
    float pitch,
    RegistryKey<World> world
){
    public DimensionalTeleportTarget(Vec3d pos, float yaw, float pitch, World world){
        this(pos, yaw, pitch, world.getRegistryKey());
    }
    
    public DimensionalTeleportTarget(Vec3d pos, float yaw, float pitch, Identifier world){
        this(pos, yaw, pitch, RegistryKey.of(Registry.WORLD_KEY, world));
    }
    
    public TeleportTarget toTeleportTarget(Entity entity){
        return new TeleportTarget(
            position,
            entity.getVelocity(),
            yaw, pitch
        );
    }
    
    public NbtCompound toNbt(){
        var tag = new NbtCompound();
        tag.putDouble("x", position.x);
        tag.putDouble("y", position.y);
        tag.putDouble("z", position.z);
        tag.putFloat("yaw", yaw);
        tag.putFloat("pitch", pitch);
        // Manifold doesn't see this?
        tag.putString("world", world.getValue().toString());
        return tag;
    }
    
    public static DimensionalTeleportTarget fromNbt(NbtCompound tag){
        return new DimensionalTeleportTarget(
            new Vec3d(
                tag.getDouble("x"),
                tag.getDouble("y"),
                tag.getDouble("z")
            ),
            tag.getFloat("yaw"),
            tag.getFloat("pitch"),
            // Manifold doesn't see this?
            new Identifier(tag.getString("world"))
        );
    }
}
