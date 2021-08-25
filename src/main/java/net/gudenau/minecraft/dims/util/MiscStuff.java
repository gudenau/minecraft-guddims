package net.gudenau.minecraft.dims.util;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.gudenau.minecraft.dims.accessor.*;
import net.gudenau.minecraft.dims.accessor.client.NativeImageAccessor;
import net.gudenau.minecraft.dims.api.v0.util.DimensionalTeleportTarget;
import net.gudenau.minecraft.dims.duck.EntityDuck;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DyeColor;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.timer.Timer;
import net.minecraft.world.timer.TimerCallbackSerializer;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryUtil;

public final class MiscStuff{
    private MiscStuff(){}
    
    public static void teleportEntity(Entity entity, DimensionalTeleportTarget target){
        // Make sure this is server side
        if(entity.world.isClient()){
            throw new RuntimeException("Entity.teleportToTarget called on client");
        }
        var destWorld = target.world();
        var pos = target.position();
        // Check to see if the entity is in the same dimension as the target
        if(entity.world.getRegistryKey().equals(destWorld)){
            // That's easy, just move them
            entity.teleport(pos.x, pos.y, pos.z);
            entity.setYaw(target.yaw());
            entity.setPitch(target.pitch());
        }else{
            // Get the new world
            var newWorld = entity.world.getServer().getWorld(target.world());
            if(newWorld == null){
                // Woops!
                throw new RuntimeException("Failed to teleport entity " + entity + ": destination world was null");
            }
            // Set the teleport target and ask vanilla code to TP
            ((EntityDuck)entity).gud_dims$setTeleportTarget(target.toTeleportTarget(entity));
            entity.moveToWorld(newWorld);
        }
    }
    
    /**
     * Creates a Timer instance from an NBT tag
     *
     * @param list The tag to read
     * @param serializer The serializer for the Timer
     * @param <T> The type of Timer
     * @return The read Timer
     */
    public static <T> Timer<T> timerFromNbt(NbtList list, TimerCallbackSerializer<T> serializer){
        var timer = new Timer<>(serializer);
        @SuppressWarnings("ConstantConditions")
        var timerAccessor = (TimerAccessor)timer;
        list.stream()
            .map((tag)->(NbtCompound)tag)
            .forEach(timerAccessor::invokeAddEvent);
        return timer;
    }
    
    // Mojank removed the int field for the color....
    private static final Object2IntMap<DyeColor> COLORS;
    static{
        var colors = new Object2IntOpenHashMap<DyeColor>(DyeColor.values().length);
        for(var value : DyeColor.values()){
            var components = value.getColorComponents();
            colors.put(value, ((int)(components[0] * 255) << 16) | ((int)(components[1] * 255) << 8) | (int)(components[2] * 255));
        }
        COLORS = Object2IntMaps.unmodifiable(colors);
    }
    
    /**
     * Gets the raw color of the dye.
     *
     * @return The color of the dye
     */
    public static int getDyeColor( DyeColor thiz) {
        return COLORS.getInt(thiz);
    }
    
    /**
     * A helper to write world border information to a tag.
     *
     * @return The written tag
     */
    public static NbtCompound worldBorderToNbt(WorldBorder.Properties thiz) {
        var compound = new NbtCompound();
        thiz.writeNbt(compound);
        return compound;
    }
    
    /**
     * Creates world border properties from a tag.
     *
     * @param tag The tag to read
     * @return The created properties
     */
    public static WorldBorder.Properties worldBorderFromNbt(NbtCompound tag){
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
    
    @Environment(EnvType.CLIENT)
    public static IntBuffer getImagePixels(NativeImage image){
        @SuppressWarnings("ConstantConditions")
        var accessor = (NativeImageAccessor)(Object)image;
        var pointer = accessor.getPointer();
        var sizeBytes = accessor.getSizeBytes();
        return MemoryUtil.memByteBuffer(pointer, (int)sizeBytes)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer();
    }
    
    // TODO Figure out a better way to do this
    public static String sanitizeName(String string){
        StringBuilder builder = new StringBuilder();
        string.chars().forEachOrdered((character)->{
            if(character >= 'A' && character <= 'Z'){
                character |= 0b0100000;
            }
            if(
                (character >= 'a' && character <= 'z') ||
                (character >= '0' && character <= '9') ||
                character == '.' ||
                character == '_'
            ){
                builder.append(Character.toString(character));
            }else{
                builder.append('_');
            }
        });
        
        return builder.toString();
    }
    
    public static @NotNull Path getSavePath(@NotNull MinecraftServer server){
        return ((LevelStorage$SessionAccessor)(((MinecraftServerAccessor)server).getSession())).getDirectory().resolve("gud").resolve("dims");
    }
    
    public static void delete(Path path) throws IOException{
        if(!Files.exists(path)){
            return;
        }
        if(Files.isRegularFile(path)){
            Files.deleteIfExists(path);
        }else{
            try(var stream = Files.walk(path)){
                stream
                    .sorted((fileA, fileB)->{
                        boolean fileAFile = Files.isRegularFile(fileA);
                        boolean fileBFile = Files.isRegularFile(fileB);
                        if(fileAFile == fileBFile){
                            if(!fileAFile){
                                if(fileA.startsWith(fileB)){
                                    return -1;
                                }else if(fileB.startsWith(fileA)){
                                    return 1;
                                }
                            }
                            return 0;
                        }else if(fileAFile){
                            return -1;
                        }else{
                            return 1;
                        }
                    })
                    .forEachOrdered((file)->{
                        try{
                            Files.deleteIfExists(file);
                        }catch(IOException e){
                            throw new UncheckedIOException(e);
                        }
                    });
            }catch(UncheckedIOException e){
                throw e.getCause();
            }
        }
    }
}
