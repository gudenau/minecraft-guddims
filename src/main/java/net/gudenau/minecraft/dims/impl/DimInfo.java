package net.gudenau.minecraft.dims.impl;

import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.util.NbtType;
import net.gudenau.minecraft.dims.accessor.MinecraftServerAccessor;
import net.gudenau.minecraft.dims.accessor.WorldAccessor;
import net.gudenau.minecraft.dims.api.v0.DimRegistry;
import net.gudenau.minecraft.dims.api.v0.attribute.*;
import net.gudenau.minecraft.dims.api.v0.controller.WeatherDimController;
import net.gudenau.minecraft.dims.api.v0.util.collection.ObjectIntPair;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.HorizontalVoronoiBiomeAccessType;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Spawner;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.level.ServerWorldProperties;
import org.jetbrains.annotations.Nullable;

import static net.gudenau.minecraft.dims.Dims.MOD_ID;

/**
 * Information about a dimension. Also used to create the world instances.
 *
 * @since 0.0.1
 */
final class DimInfo{
    private static final DimRegistry DIM_REGISTRY = DimRegistry.getInstance();
    private static final Random RANDOM = new Random(System.nanoTime() ^ 0xDEADC0DEDEADBEEFL);
    
    private final UUID uuid;
    private final String name;
    private final List<DimAttribute> attributes;
    private final RegistryKey<World> registryKey;
    private final RegistryKey<DimensionType> dimensionTypeKey;
    private final Random random;
    
    private BiomeSource biomeSource;
    private DimensionWorldProperties worldProps;
    private DimensionType dimensionType;
    
    DimInfo(MinecraftServer server, NbtCompound tag){
        uuid = tag.getUuid("uuid");
        name = tag.getString("name");
        random = new Random(uuid.getLeastSignificantBits() ^ uuid.getMostSignificantBits() ^ System.currentTimeMillis());
        
        var attributes = new ArrayList<DimAttribute>();
        var attributeList = tag.getList("attributes", NbtType.COMPOUND);
        for(var element : attributeList){
            var attributeTag = (NbtCompound)element;
            var type = DIM_REGISTRY.getAttributeType(new Identifier(attributeTag.getString("type")))
                .orElseThrow(()->new RuntimeException("Unknown type: " + attributeTag.getString("type")));
            attributes.add(DIM_REGISTRY.getAttribute(type, new Identifier(attributeTag.getString("attribute")))
                .orElseThrow(()->new RuntimeException("Unknown attribute: " + attributeTag.getString("attribute"))));
        }
        this.attributes = Collections.unmodifiableList(attributes);
        
        //TODO Make this NBT based
        parseAttributes(server, attributes);
        
        worldProps = DimensionWorldProperties.fromNbt(
            random,
            tag.getCompound("props"),
            (ServerWorldProperties)server.getOverworld().getLevelProperties(),
            name,
            server.getGameRules()
        );
        
        registryKey = RegistryKey.of(Registry.WORLD_KEY, new Identifier(MOD_ID, getName()));
        dimensionTypeKey = RegistryKey.of(Registry.DIMENSION_TYPE_KEY, new Identifier(MOD_ID, getName()));
        // create(OptionalLong.empty(), true, false, false, true, 1.0D, false, false, true, false, true, 0, 256, 256, HorizontalVoronoiBiomeAccessType.INSTANCE, BlockTags.INFINIBURN_OVERWORLD.getId(), OVERWORLD_ID, 0.0F);
        dimensionType = loadDimType(server, tag.getCompound("dimType"));
    }
    
    public DimInfo(MinecraftServer server, UUID uuid, String name, List<DimAttribute> attributes){
        this.uuid = uuid;
        this.name = name;
        random = new Random(uuid.getLeastSignificantBits() ^ uuid.getMostSignificantBits() ^ System.currentTimeMillis());
        registryKey = RegistryKey.of(Registry.WORLD_KEY, new Identifier(MOD_ID, getName()));
        dimensionTypeKey = RegistryKey.of(Registry.DIMENSION_TYPE_KEY, new Identifier(MOD_ID, getName()));
        parseAttributes(server, attributes);
        this.attributes = List.copyOf(attributes);
    }
    
    /**
     * Parses attributes to figure out how this dimension should behave.
     *
     * Also fills in missing attributes with the power of RNG.
     *
     * TODO We need to add instability to try and balance this a touch
     *
     * @param server The server instance
     * @param attributes The attributes to parse
     */
    private void parseAttributes(MinecraftServer server, List<DimAttribute> attributes){
        //FIXME Make garbage attributes have a cost
        List<DimAttribute> garbage = new ArrayList<>();
        
        // A somewhat messy feeling way to take the list an turn it into a map
        Map<DimAttributeType, List<DimAttribute>> attributeMap = new Object2ObjectOpenHashMap<>();
        for(int i = 0; i < attributes.size(); i++){
            var currentAttribute = attributes.get(i);
            // If we don't have a controller and the attribute isn't a controller itself, it's garbage
            if(!(currentAttribute instanceof ControllerDimAttribute controllerAttribute)){
                garbage.add(currentAttribute);
                continue;
            }
            var controller = controllerAttribute.getController();
            
            // If the attribute is a controller and was already seen, it is also garbage
            var type = currentAttribute.getType();
            if(attributeMap.containsKey(type)){
                garbage.add(currentAttribute);
                continue;
            }
            
            // Now we are getting somewhere, we have a controller
            var appliedAttributes = new ArrayList<DimAttribute>();
            appliedAttributes.add(controllerAttribute); // Not a bug, required for parsing later
            
            // Iterate over attributes until we reach the end
            for(i++; i < attributes.size(); i++){
                currentAttribute = attributes.get(i);
                // This attribute is valid, add it to the attribute list, otherwise break.
                if(controller.isPropertyValid(currentAttribute)){
                    appliedAttributes.add(currentAttribute);
                }else{
                    i--;
                    break;
                }
            }
            // Record the controller and it's attributes
            attributeMap.put(type, appliedAttributes);
        }
    
        long instability = garbage.size();
        
        // And create the dimension stuff
        var biomeSourcePair = createBiomeSource(attributeMap.get(DimAttributeType.BIOME_CONTROLLER));
        biomeSource = biomeSourcePair.object();
        instability += biomeSourcePair.integer();
        worldProps = createWorldProperties(server, attributeMap);
    
        dimensionType = createDimType(server, attributeMap);
    }
    
    /**
     * Creates dimension props based on provided attributes.
     *
     * @param server The server instance
     * @param attributeMap The read attributes from parseAttributes
     * @return The dimension props
     */
    private DimensionWorldProperties createWorldProperties(MinecraftServer server, Map<DimAttributeType, List<DimAttribute>> attributeMap){
        var gameRules = new DimensionGameRules(server.getGameRules());
        
        return new DimensionWorldProperties(
            (ServerWorldProperties)server.getOverworld().getLevelProperties(),
            name,
            createWeatherController(random, attributeMap.get(DimAttributeType.WEATHER)),
            gameRules
        );
    }
    
    /**
     * Creates a new weather controller from the provided attributes.
     *
     * @param random The world's random
     * @param attributes The weather attributes
     * @return The new weather controller
     */
    private WeatherDimController.WeatherController createWeatherController(Random random, List<DimAttribute> attributes){
        WeatherDimController controller;
        if(attributes == null || attributes.isEmpty()){
            controller = DIM_REGISTRY.<WeatherDimAttribute>getRandomAttribute(DimAttributeType.WEATHER).getController();
        }else{
            controller = ((WeatherDimAttribute)attributes.get(0)).getController();
        }
        return controller.createController(random, attributes == null ? List.of() : attributes);
    }
    
    /**
     * Creates the biome source for a dimension.
     *
     * @param attributes The attributes to parse
     * @return The biome source from the attributes
     */
    private ObjectIntPair<BiomeSource> createBiomeSource(@Nullable List<DimAttribute> attributes){
        // Well we need to make everything up if not supplied
        if(attributes == null){
            attributes = new ArrayList<>();
            // Pick a random controller
            BiomeControllerDimAttribute controller = DIM_REGISTRY.getRandomAttribute(DimAttributeType.BIOME_CONTROLLER);
            attributes.add(controller);
        }
        
        var controller = ((BiomeControllerDimAttribute)attributes.remove(0)).getController();
        List<Biome> biomeList;
        if(attributes.size() == 0){
            biomeList = controller.generateBiomeList(RANDOM);
        }else{
            biomeList = attributes.stream()
                .map((attribute)->(BiomeDimAttribute)attribute)
                .map(BiomeDimAttribute::getBiome)
                .collect(Collectors.toList());
        }
        
        var biomeRange = controller.getValidBiomeCount();

        // TODO Penalize removing and adding biomes at this stage
        // Make sure their are just the right amount of biomes
        while(biomeRange.isOver(biomeList.size())){
            biomeList.add(DIM_REGISTRY.<BiomeDimAttribute>getRandomAttribute(DimAttributeType.BIOME).getBiome());
        }
        while(biomeRange.isUnder(biomeList.size())){
            biomeList.remove(biomeList.size() - 1);
        }
        
        return controller.createBiomeSource(biomeList);
    }
    
    /**
     * Creates a custom dimension type.
     *
     * TODO Make properties for hard coded values
     *
     * @param server The server instance
     * @param attributeMap The user provided attributes
     * @return The new dimension type
     */
    private DimensionType createDimType(MinecraftServer server, Map<DimAttributeType, List<DimAttribute>> attributeMap){
        var type = DimensionType.create(
            OptionalLong.empty(),
            getBooleanAttribute(attributeMap, DimAttributeType.SKYLIGHT),
            false,
            false,
            false,
            1,
            false,
            false,
            false,
            false,
            false,
            0,
            256,
            256,
            HorizontalVoronoiBiomeAccessType.INSTANCE,
            BlockTags.INFINIBURN_OVERWORLD.getId(),
            DimensionType.OVERWORLD_ID,
            0
        );
        server.getRegistryManager().getMutable(Registry.DIMENSION_TYPE_KEY).add(dimensionTypeKey, type, Lifecycle.stable());
        return type;
    }
    
    /**
     * Gets or generates a boolean value from a controller type.
     *
     * @param attributeMap The map of attributes
     * @param type The type of controller
     * @return True or false
     */
    private boolean getBooleanAttribute(Map<DimAttributeType, List<DimAttribute>> attributeMap, DimAttributeType type){
        return Optional.ofNullable(attributeMap.get(type))
            .map((list)->(BooleanDimAttribute)(list.isEmpty() ? null : list.get(1)))
            .orElseGet(()->DIM_REGISTRY.getRandomAttribute(DimAttributeType.BOOLEAN))
            .getBoolean();
    }
    
    /**
     * Leads the dimension type from a tag.
     *
     * @param server The service instance
     * @param tag The dim type tag
     * @return The new dim type
     */
    private DimensionType loadDimType(MinecraftServer server, NbtCompound tag){
        var type = DimensionType.create(
            OptionalLong.empty(),
            tag.getBoolean("skylight"),
            tag.getBoolean("ceiling"),
            tag.getBoolean("ultrawarm"),
            tag.getBoolean("natural"),
            tag.getDouble("scale"),
            tag.getBoolean("hasDragon"),
            tag.getBoolean("piglinSafe"),
            tag.getBoolean("bedWorks"),
            tag.getBoolean("anchorWorks"),
            tag.getBoolean("hasRaids"),
            tag.getInt("minimumY"),
            tag.getInt("height"),
            tag.getInt("logicalHeight"),
            HorizontalVoronoiBiomeAccessType.INSTANCE,
            BlockTags.INFINIBURN_OVERWORLD.getId(),
            DimensionType.OVERWORLD_ID,
            0
        );
        server.getRegistryManager().getMutable(Registry.DIMENSION_TYPE_KEY).add(dimensionTypeKey, type, Lifecycle.stable());
        return type;
    }
    
    /**
     * Writes this dim info to an NBT tag for serialization.
     *
     * @return The compound tag
     */
    NbtCompound toNbt(){
        var tag = new NbtCompound();
        
        tag.putUuid("uuid", uuid);
        
        tag.putString("name", name);
        
        var attributeList = new NbtList();
        for(DimAttribute attribute : attributes){
            var compound = new NbtCompound();
            compound.putString("type", attribute.getType().getId().toString());
            compound.putString("attribute", attribute.getId().toString());
            attributeList.add(compound);
        }
        tag.put("attributes", attributeList);
        
        tag.put("props", worldProps.toNbt());
    
        var typeTag = new NbtCompound();
        typeTag.putBoolean("skylight", dimensionType.hasSkyLight());
        typeTag.putBoolean("ceiling", dimensionType.hasCeiling());
        typeTag.putBoolean("ultrawarm", dimensionType.isUltrawarm());
        typeTag.putBoolean("natural", dimensionType.isNatural());
        typeTag.putDouble("scale", dimensionType.getCoordinateScale());
        typeTag.putBoolean("hasDragon", dimensionType.hasEnderDragonFight());
        typeTag.putBoolean("piglinSafe", dimensionType.isPiglinSafe());
        typeTag.putBoolean("bedWorks", dimensionType.isBedWorking());
        typeTag.putBoolean("anchorWorks", dimensionType.isRespawnAnchorWorking());
        typeTag.putBoolean("hasRaids", dimensionType.hasRaids());
        typeTag.putInt("minimumY", dimensionType.getMinimumY());
        typeTag.putInt("height", dimensionType.getHeight());
        typeTag.putInt("logicalHeight", dimensionType.getLogicalHeight());
        tag.put("dimType", typeTag);
        
        return tag;
    }
    
    /**
     * Gets the UUID that is associated with this dimension info.
     *
     * @return The associated UUID
     */
    public UUID getUuid(){
        return uuid;
    }
    
    /**
     * Gets the name of the dimension that this dimension info represents.
     *
     * @return The name of the dimension
     */
    public String getName(){
        return name;
    }
    
    /**
     * Creates a new instance of ServerWorld based off of this dimension info.
     *
     * @param server The server instance
     * @return The new world
     */
    public ServerWorld createWorld(MinecraftServer server){
        var registryManager = server.getRegistryManager();
    
        //TODO Make this visible to the user
        WorldGenerationProgressListener progressListener = new WorldGenerationProgressListener(){
            @Override
            public void start(ChunkPos spawnPos){
                System.out.println("Starting gen on " + name + ", spawn is in " + spawnPos.toString());
            }
    
            @Override
            public void setChunkStatus(ChunkPos pos, @Nullable ChunkStatus status){}
    
            @Override
            public void start(){}
    
            @Override
            public void stop(){}
        };
        
        long seed = uuid.getLeastSignificantBits() ^ uuid.getMostSignificantBits();
        ChunkGenerator chunkGenerator = new NoiseChunkGenerator(
            biomeSource,
            seed,
            ()->registryManager.get(Registry.CHUNK_GENERATOR_SETTINGS_KEY).getOrThrow(ChunkGeneratorSettings.OVERWORLD)
        );
        List<Spawner> entitySpawners = List.of();
        
        var world = new ServerWorld(
            server,
            ((MinecraftServerAccessor)server).getWorkerExecutor(),
            ((MinecraftServerAccessor)server).getSession(),
            worldProps,
            getRegistryKey(),
            dimensionType,
            progressListener,
            chunkGenerator,
            false,
            seed,
            entitySpawners,
            false // Handled elsewhere
        ){
            @Override
            public void tick(BooleanSupplier shouldKeepTicking){
                worldProps.tick();
                super.tick(shouldKeepTicking);
            }
        };
        ((WorldAccessor)(Object)world).setRandom(random);
        return world;
    }
    
    /**
     * Gets the world registry key associated with this dimension info.
     *
     * @return The associated registry key
     */
    public RegistryKey<World> getRegistryKey(){
        return registryKey;
    }
}
