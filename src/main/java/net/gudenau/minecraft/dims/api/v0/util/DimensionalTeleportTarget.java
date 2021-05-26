package net.gudenau.minecraft.dims.api.v0.util;

import java.util.Objects;
import net.gudenau.minecraft.dims.api.v0.util.collection.ObjectIntPair;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

/**
 * A basic cross-dimensional teleport target.
 *
 * @since 0.0.1
 */
public record DimensionalTeleportTarget(
    Vec3d position,
    float yaw,
    float pitch,
    RegistryKey<World> world
){
    /**
     * Creates a new teleport target with the provided position, yaw, pitch and world.
     *
     * This is the same as new DimensionTeleportTarget(pos, yaw, pitch, world.getRegistryKey())
     *
     * @param pos The destination position
     * @param yaw The destination yaw
     * @param pitch The destination pitch
     * @param world The destination world
     */
    public DimensionalTeleportTarget(Vec3d pos, float yaw, float pitch, World world){
        this(pos, yaw, pitch, Objects.requireNonNull(world, "world was null").getRegistryKey());
    }
    
    /**
     * Creates a new teleport target with the provided position, yaw, pitch and world identifier.
     *
     * This is the same as new DimensionTeleportTarget(pos, yaw, pitch, RegistryKey.of(Registry.WORLD_KEY, world))
     *
     * @param pos The destination position
     * @param yaw The destination yaw
     * @param pitch The destination pitch
     * @param world The destination world identifier
     */
    public DimensionalTeleportTarget(Vec3d pos, float yaw, float pitch, Identifier world){
        this(pos, yaw, pitch, RegistryKey.of(Registry.WORLD_KEY, Objects.requireNonNull(world, "world was null")));
    }
    
    public DimensionalTeleportTarget(Vec3d position, float yaw, float pitch, RegistryKey<World> world){
        this.position = Objects.requireNonNull(position, "position was null");
        this.yaw = yaw;
        this.pitch = pitch;
        this.world = Objects.requireNonNull(world, "world was null");
    }
    
    /**
     * Creates a vanilla teleportation target for use when actually teleporting an entity.
     *
     * TODO Make this redirect entity velocity
     *
     * @param entity The entity that is being teleported
     * @return The teleport target
     */
    public TeleportTarget toTeleportTarget(Entity entity){
        return new TeleportTarget(
            position,
            entity.getVelocity(),
            yaw, pitch
        );
    }
    
    /**
     * Writes this target to a compound NBT tag.
     *
     * @return The created tag
     */
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
    
    /**
     * Creates a dimensional teleport target from an NBT compound tag.
     *
     * @param tag The tag to read
     * @return The deserialized target
     */
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
