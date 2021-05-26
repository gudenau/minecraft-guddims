package net.gudenau.minecraft.dims;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.gudenau.minecraft.dims.api.v0.attribute.BiomeDimAttribute;
import net.gudenau.minecraft.dims.api.v0.attribute.BlockDimAttribute;
import net.gudenau.minecraft.dims.api.v0.attribute.FluidDimAttribute;
import net.gudenau.minecraft.dims.client.BlockColorCache;
import net.gudenau.minecraft.dims.client.renderer.blockentity.PortalBlockEntityRenderer;
import net.gudenau.minecraft.dims.item.DimensionAttributeItem;
import net.minecraft.client.color.world.BiomeColors;
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
    }
    
    private void registerPackets(){
        ClientPlayNetworking.registerGlobalReceiver(Dims.Packets.REGISTER_DIM, (client, handler, buffer, sender)->{
            client.getNetworkHandler().getWorldKeys().add(RegistryKey.of(Registry.WORLD_KEY, buffer.readIdentifier()));
            //Registry.DIMENSION_TYPE_KEY
        });
    }
    
    private void registerBlockEntityRenderers(){
        var registry = BlockEntityRendererRegistry.INSTANCE;
        registry.register(Dims.Blocks.Entities.PORTAL, (ctx)->new PortalBlockEntityRenderer());
    }
    
    private void registerItemColors(){
        var registry = ColorProviderRegistry.ITEM;
        
        registry.register((stack, tintIndex)->{
            var attribute = DimensionAttributeItem.getAttribute(stack);
            return attribute.map((value)->{
                var biome = ((BiomeDimAttribute)value).getBiome();
                return switch(tintIndex){
                    case 0 -> BiomeColors.FOLIAGE_COLOR.getColor(biome, 0, 0);
                    case 1 -> BiomeColors.GRASS_COLOR.getColor(biome, 0, 0);
                    case 2 -> BiomeColors.WATER_COLOR.getColor(biome, 0, 0);
                    case 3 -> biome.getSkyColor();
                    default -> 0xFFFFFFFF;
                };
            }).orElse(0xFFFFFFFF);
        }, Dims.Items.DIMENSION_ATTRIBUTE_BIOME);
        
        registry.register((stack, tintIndex)->{
            var attribute = DimensionAttributeItem.getAttribute(stack);
            return attribute.map((value)->{
                var block = ((BlockDimAttribute)value).getBlock();
                var colors = BlockColorCache.getColors(block);
                return switch(tintIndex){
                    case 0 -> colors.a();
                    case 1 -> colors.b();
                    case 2 -> colors.c();
                    default -> -1;
                };
            }).orElse(0xFFFFFFFF);
        }, Dims.Items.DIMENSION_ATTRIBUTE_BLOCK);
    
        registry.register((stack, tintIndex)->{
            if(tintIndex != 0){
                return 0xFFFFFFFF;
            }
        
            var attribute = DimensionAttributeItem.getAttribute(stack);
            return attribute.map((value)->{
                var state = ((FluidDimAttribute)value).getFluid().getDefaultState().getBlockState();
                return state.getMapColor(null, null).color;
            }).orElse(0xFFFFFFFF);
        }, Dims.Items.DIMENSION_ATTRIBUTE_FLUID);
    }
}
