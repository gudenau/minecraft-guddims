package net.gudenau.minecraft.dims;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.model.ModelVariantProvider;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.gudenau.minecraft.dims.api.v0.attribute.*;
import net.gudenau.minecraft.dims.client.BlockColorCache;
import net.gudenau.minecraft.dims.client.model.AttributeModelProvider;
import net.gudenau.minecraft.dims.client.renderer.blockentity.PortalBlockEntityRenderer;
import net.gudenau.minecraft.dims.impl.client.SkyRegistry;
import net.gudenau.minecraft.dims.item.DimensionAttributeItem;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

/**
 * The client entry point for this mod.
 *
 * @since 0.0.1
 */
@Environment(EnvType.CLIENT)
public final class DimsClient implements ClientModInitializer{
    @Override
    public void onInitializeClient(){
        registerBlockEntityRenderers();
        registerItemColors();
        registerPackets();
        registerEventHandlers();
        registerModelStuff();
    }
    
    private void registerModelStuff(){
        var registry = ModelLoadingRegistry.INSTANCE;
        registry.registerResourceProvider((manager)->new AttributeModelProvider());
    }
    
    private void registerEventHandlers(){
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client)->SkyRegistry.reset());
        ClientLoginConnectionEvents.DISCONNECT.register((handler, client)->SkyRegistry.reset());
    }
    
    private void registerPackets(){
        ClientPlayNetworking.registerGlobalReceiver(Dims.Packets.REGISTER_DIM, (client, handler, buffer, sender)->
            client.getNetworkHandler().getWorldKeys().add(RegistryKey.of(Registry.WORLD_KEY, buffer.readIdentifier()))
        );
        ClientPlayNetworking.registerGlobalReceiver(Dims.Packets.REGISTER_SKY, (client, handler, buffer, sender)->
            SkyRegistry.getInstance().registerSky(buffer)
        );
    }
    
    private void registerBlockEntityRenderers(){
        var registry = BlockEntityRendererRegistry.INSTANCE;
        registry.register(Dims.Blocks.Entities.PORTAL, (ctx)->new PortalBlockEntityRenderer());
    }
    
    private void registerItemColors(){
        var registry = ColorProviderRegistry.ITEM;
        
        registry.register((stack, tintIndex)->{
            var attribute = DimensionAttributeItem.getAttribute(stack);
            return attribute.map((value)->
                switch(value.getType()){
                    case BIOME -> {
                        var biome = ((BiomeDimAttribute)value).getBiome();
                        yield switch(tintIndex){
                            case 0 -> BiomeColors.FOLIAGE_COLOR.getColor(biome, 0, 0);
                            case 1 -> BiomeColors.GRASS_COLOR.getColor(biome, 0, 0);
                            case 2 -> BiomeColors.WATER_COLOR.getColor(biome, 0, 0);
                            case 3 -> biome.getSkyColor();
                            default -> 0xFFFFFFFF;
                        };
                    }
                    case BLOCK -> {
                        var block = ((BlockDimAttribute)value).getBlock();
                        var colors = BlockColorCache.getColors(block);
                        yield switch(tintIndex){
                            case 0 -> colors.a();
                            case 1 -> colors.b();
                            case 2 -> colors.c();
                            default -> 0xFFFFFFF;
                        };
                    }
                    case COLOR -> tintIndex == 1 ? ((ColorDimAttribute)value).getColorValue() : -1;
                    case FEATURE ->{
                        var id = value.getId();
                        var hash = id.toString().hashCode();
                        yield switch(tintIndex){
                            case 0 -> MathHelper.hsvToRgb((hash & 0xFF) / 255F, 1, 1);
                            case 1 -> MathHelper.hsvToRgb(((hash >>> 8) & 0xFF) / 255F, 1, 1);
                            case 2 -> MathHelper.hsvToRgb(((hash >>> 16) & 0xFF) / 255F, 1, 1);
                            case 3 -> MathHelper.hsvToRgb((hash >>> 24) / 255F, 1, 1);
                            default -> 0xFFFFFFFF;
                        };
                    }
                    case FLUID ->{
                        var state = ((FluidDimAttribute)value).getFluid().getDefaultState().getBlockState();
                        yield tintIndex != 0 ? 0xFFFFFFFF : state.getMapColor(null, null).color;
                    }
                    default -> 0xFFFFFFFF;
                }
            ).orElse(0xFFFFFFFF);
        }, Dims.Items.DIMENSION_ATTRIBUTE);
    }
}
