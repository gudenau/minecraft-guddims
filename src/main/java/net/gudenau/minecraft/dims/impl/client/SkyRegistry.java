package net.gudenau.minecraft.dims.impl.client;

import java.util.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.gudenau.minecraft.dims.Dims;
import net.gudenau.minecraft.dims.api.v0.DimRegistry;
import net.gudenau.minecraft.dims.api.v0.attribute.CelestialDimAttribute;
import net.gudenau.minecraft.dims.api.v0.controller.CelestialDimController;
import net.gudenau.minecraft.dims.api.v0.controller.ControllerType;
import net.gudenau.minecraft.dims.impl.DimInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.SkyProperties;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;

/**
 * FIXME Yeah this might need to move and have things change.
 *
 * @since 0.0.4
 */
public final class SkyRegistry{
    private SkyRegistry(){}
    
    @Environment(EnvType.CLIENT)
    private static final DimRegistry DIM_REGISTRY = DimRegistry.getInstance();
    
    @Environment(EnvType.CLIENT)
    private static SkyRegistry INSTANCE = new SkyRegistry();
    
    @Environment(EnvType.CLIENT)
    public static SkyRegistry getInstance(){
        return INSTANCE;
    }
    
    public static Packet<?> createPacket(DimInfo info){
        var buffer = PacketByteBufs.create();
        buffer.writeIdentifier(info.getDimensionTypeKey().getValue());
        
        var objects = info.getCelestialObjects();
        buffer.writeVarInt(objects.size());
        objects.forEach((object)->{
            buffer.writeIdentifier(object.getId());
            object.serialize(buffer);
        });
        
        return ServerPlayNetworking.createS2CPacket(Dims.Packets.REGISTER_SKY, buffer);
    }
    
    @Environment(EnvType.CLIENT)
    private final Map<DimensionType, SkyProperties> skyProperties = new HashMap<>();
    
    public static void reset(){
        INSTANCE = new SkyRegistry();
    }
    
    @Environment(EnvType.CLIENT)
    public Optional<SkyProperties> getSkyProperties(DimensionType dimensionType){
        return Optional.ofNullable(skyProperties.get(dimensionType));
    }
    
    @Environment(EnvType.CLIENT)
    public void registerSky(PacketByteBuf buffer){
        var type = MinecraftClient.getInstance().getServer().getRegistryManager()
            .getMutable(Registry.DIMENSION_TYPE_KEY).get(buffer.readIdentifier());
        int count = buffer.readVarInt();
        List<CelestialDimController.CelestialObject> objects = new ArrayList<>(count);
        for(int i = 0; i < count; i++){
            var id = buffer.readIdentifier();
            var controller = DIM_REGISTRY.<CelestialDimAttribute>getController(ControllerType.CELESTIAL, id)
                .orElseThrow(()->new RuntimeException("Unknown celestial object id " + id));
            objects.add(controller.getController().deserialize(buffer));
        }
        skyProperties.put(type, new DimensionSkyPropertiesImpl(objects));
    }
}
