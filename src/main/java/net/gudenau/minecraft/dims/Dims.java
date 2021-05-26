package net.gudenau.minecraft.dims;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.gudenau.minecraft.dims.api.v0.DimRegistry;
import net.gudenau.minecraft.dims.api.v0.attribute.DimAttributeType;
import net.gudenau.minecraft.dims.block.*;
import net.gudenau.minecraft.dims.block.entity.DimensionBuilderBlockEntity;
import net.gudenau.minecraft.dims.block.entity.PortalBlockEntity;
import net.gudenau.minecraft.dims.block.entity.PortalReceptacleBlockEntity;
import net.gudenau.minecraft.dims.impl.DimRegistryImpl;
import net.gudenau.minecraft.dims.item.DimensionAnchorItem;
import net.gudenau.minecraft.dims.item.DimensionAttributeItem;
import net.gudenau.minecraft.dims.item.DimensionTokenItem;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.*;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.property.IntProperty;
import net.minecraft.tag.Tag;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * The server/client entry point for this mod.
 *
 * @since 0.0.1
 */
public final class Dims implements ModInitializer{
    public static final String MOD_ID = "gud_dims";
    
    @Override
    public void onInitialize(){
        Blocks.init();
        Items.init();
        Tags.init();
    
        ServerLifecycleEvents.SERVER_STARTING.register(DimRegistryImpl.INSTANCE::init);
        ServerLifecycleEvents.SERVER_STARTED.register(DimRegistryImpl.INSTANCE::createWorlds);
        ServerLifecycleEvents.SERVER_STOPPED.register((server)->{
            try{
                DimRegistryImpl.INSTANCE.saveWorlds(server);
            }finally{
                DimRegistryImpl.INSTANCE.deinit(server);
            }
        });
        ServerTickEvents.START_SERVER_TICK.register(DimRegistryImpl.INSTANCE::addWorlds);
    }
    
    public static final class Blocks{
        public static final Block ANCHOR_MINTER = new AnchorMinterBlock(
            FabricBlockSettings.of(Material.GLASS, MapColor.LIGHT_BLUE).strength(3).sounds(BlockSoundGroup.GLASS).luminance((state)->15)
        );
        public static final Block DIMENSION_BUILDER = new DimensionBuilderBlock(
            FabricBlockSettings.of(Material.GLASS, MapColor.LIGHT_BLUE).strength(3).sounds(BlockSoundGroup.GLASS).luminance((state)->15)
        );
        public static final Block PRISCILLITE = new Block(
            FabricBlockSettings.of(Material.GLASS, DyeColor.LIGHT_BLUE).strength(0.3F).sounds(BlockSoundGroup.GLASS).luminance((state)->15)
        );
        public static final Block PORTAL = new PortalBlock(
            FabricBlockSettings.of(Material.PORTAL).noCollision().strength(-1).sounds(BlockSoundGroup.GLASS).luminance((state)->16)
        );
        public static final Block PORTAL_RECEPTACLE = new PortalReceptacleBlock(
            FabricBlockSettings.of(Material.GLASS, MapColor.LIGHT_BLUE).strength(3).sounds(BlockSoundGroup.GLASS).luminance((state)->15)
        );
        
        private static void register(String name, Block block){
            Registry.register(Registry.BLOCK, new Identifier(MOD_ID, name), block);
        }
        
        private static void init(){
            register("anchor_minter", ANCHOR_MINTER);
            register("dimension_builder", DIMENSION_BUILDER);
            register("priscillite", PRISCILLITE);
            register("portal", PORTAL);
            register("portal_receptacle", PORTAL_RECEPTACLE);
            
            Entities.init();
        }
    
        public static final class Entities{
            public static final BlockEntityType<DimensionBuilderBlockEntity> DIMENSION_BUILDER = create(DimensionBuilderBlockEntity::new, Blocks.DIMENSION_BUILDER);
            public static final BlockEntityType<PortalBlockEntity> PORTAL = create(PortalBlockEntity::new, Blocks.PORTAL);
            public static final BlockEntityType<PortalReceptacleBlockEntity> PORTAL_RECEPTACLE = create(PortalReceptacleBlockEntity::new, Blocks.PORTAL_RECEPTACLE);
    
            private static <T extends BlockEntity> BlockEntityType<T> create(FabricBlockEntityTypeBuilder.Factory<T> factory, Block... blocks){
                return FabricBlockEntityTypeBuilder.create(factory, blocks).build();
            }
            
            private static void register(String name, BlockEntityType<?> type){
                Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, name), type);
            }
            
            private static void init(){
                register("dimension_builder", DIMENSION_BUILDER);
                register("portal", PORTAL);
                register("portal_receptacle", PORTAL_RECEPTACLE);
            }
        }
    
        public static final class Properties{
            public static final IntProperty ROTATION4 = IntProperty.of("rotation", 0, 3);
        }
    }
    
    public static final class Items{
        public static final ItemGroup GROUP = FabricItemGroupBuilder.build(
            new Identifier(MOD_ID, "group"), ()->new ItemStack(Items.DIMENSION_BUILDER)
        );
        public static final ItemGroup BIOME_GROUP = FabricItemGroupBuilder.build(
            new Identifier(MOD_ID, "biomes"), ()->
                DimensionAttributeItem.getStack(DimRegistry.getInstance().getRandomAttribute(DimAttributeType.BIOME))
        );
        public static final ItemGroup CONTROLLER_GROUP = FabricItemGroupBuilder.build(
            new Identifier(MOD_ID, "controllers"), ()->
                DimensionAttributeItem.getStack(DimRegistry.getInstance().getRandomAttribute(DimAttributeType.BIOME_CONTROLLER))
        );
        public static final ItemGroup BLOCK_GROUP = FabricItemGroupBuilder.build(
            new Identifier(MOD_ID, "blocks"), ()->
                DimensionAttributeItem.getStack(DimRegistry.getInstance().getRandomAttribute(DimAttributeType.BLOCK))
        );
        public static final ItemGroup FLUID_GROUP = FabricItemGroupBuilder.build(
            new Identifier(MOD_ID, "fluids"), ()->
                DimensionAttributeItem.getStack(DimRegistry.getInstance().getRandomAttribute(DimAttributeType.FLUID))
        );
        public static final ItemGroup MISC_GROUP = FabricItemGroupBuilder.build(
            new Identifier(MOD_ID, "misc"), ()->
                DimensionAttributeItem.getStack(DimRegistry.getInstance().getRandomAttribute(
                    DimAttributeType.COLOR, DimAttributeType.DIGIT, DimAttributeType.BOOLEAN
                ))
        );
        
        public static final Item DIMENSION_ATTRIBUTE_BIOME = new DimensionAttributeItem(DimAttributeType.BIOME, new FabricItemSettings().group(BIOME_GROUP));
        public static final Item DIMENSION_ATTRIBUTE_BIOME_CONTROLLER = new DimensionAttributeItem(DimAttributeType.BIOME_CONTROLLER, new FabricItemSettings().group(CONTROLLER_GROUP));
        public static final Item DIMENSION_ATTRIBUTE_BOOLEAN = new DimensionAttributeItem(DimAttributeType.BOOLEAN, new FabricItemSettings().group(MISC_GROUP));
        public static final Item DIMENSION_ATTRIBUTE_BLOCK = new DimensionAttributeItem(DimAttributeType.BLOCK, new FabricItemSettings().group(BLOCK_GROUP));
        public static final Item DIMENSION_ATTRIBUTE_COLOR = new DimensionAttributeItem(DimAttributeType.COLOR, new FabricItemSettings().group(MISC_GROUP));
        public static final Item DIMENSION_ATTRIBUTE_DIGIT = new DimensionAttributeItem(DimAttributeType.DIGIT, new FabricItemSettings().group(MISC_GROUP));
        public static final Item DIMENSION_ATTRIBUTE_FLUID = new DimensionAttributeItem(DimAttributeType.FLUID, new FabricItemSettings().group(FLUID_GROUP));
        public static final Item DIMENSION_ATTRIBUTE_WEATHER = new DimensionAttributeItem(DimAttributeType.WEATHER, new FabricItemSettings().group(CONTROLLER_GROUP));
        
        public static final Item DIMENSION_ANCHOR = new DimensionAnchorItem(new FabricItemSettings().group(GROUP).maxCount(1));
        public static final Item DIMENSION_TOKEN = new DimensionTokenItem(new FabricItemSettings().group(GROUP).maxCount(1));
        
        public static final BlockItem ANCHOR_MINTER = new BlockItem(Blocks.ANCHOR_MINTER, new FabricItemSettings().group(GROUP));
        public static final BlockItem DIMENSION_BUILDER = new BlockItem(Blocks.DIMENSION_BUILDER, new FabricItemSettings().group(GROUP));
        public static final BlockItem PRISCILLITE = new BlockItem(Blocks.PRISCILLITE, new FabricItemSettings().group(GROUP));
        public static final BlockItem PORTAL_RECEPTACLE = new BlockItem(Blocks.PORTAL_RECEPTACLE, new FabricItemSettings().group(GROUP));
        
        private static void register(String name, Item item){
            Registry.register(Registry.ITEM, new Identifier(MOD_ID, name), item);
        }
        
        private static void register(BlockItem item){
            Registry.register(Registry.ITEM, Registry.BLOCK.getId(item.getBlock()), item);
        }
        
        private static void init(){
            register("dimension_attribute_biome", DIMENSION_ATTRIBUTE_BIOME);
            register("dimension_attribute_biome_controller", DIMENSION_ATTRIBUTE_BIOME_CONTROLLER);
            register("dimension_attribute_boolean", DIMENSION_ATTRIBUTE_BOOLEAN);
            register("dimension_attribute_block", DIMENSION_ATTRIBUTE_BLOCK);
            register("dimension_attribute_color", DIMENSION_ATTRIBUTE_COLOR);
            register("dimension_attribute_digit", DIMENSION_ATTRIBUTE_DIGIT);
            register("dimension_attribute_fluid", DIMENSION_ATTRIBUTE_FLUID);
            register("dimension_attribute_weather", DIMENSION_ATTRIBUTE_WEATHER);
            
            register("dimension_anchor", DIMENSION_ANCHOR);
            register("dimension_token", DIMENSION_TOKEN);
            
            register(DIMENSION_BUILDER);
            register(ANCHOR_MINTER);
            register(PRISCILLITE);
            register(PORTAL_RECEPTACLE);
        }
    }
    
    public static final class Tags{
        public static final Tag<Block> PORTAL_FRAME = TagRegistry.block(new Identifier(MOD_ID, "portal_frame"));
        
        // Triggers static init
        private static void init(){}
    }
    
    public static final class Packets{
        public static final Identifier REGISTER_DIM = new Identifier(MOD_ID, "register_dim");
    }
}
