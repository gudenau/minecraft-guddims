package net.gudenau.minecraft.dims.impl;

import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.util.NbtType;
import net.gudenau.minecraft.dims.accessor.MinecraftServerAccessor;
import net.gudenau.minecraft.dims.api.v0.DimRegistry;
import net.gudenau.minecraft.dims.api.v0.attribute.*;
import net.gudenau.minecraft.dims.impl.weather.WeatherController;
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
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.*;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.Spawner;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.timer.Timer;
import net.minecraft.world.timer.TimerCallbackSerializer;
import org.jetbrains.annotations.Nullable;

import static net.gudenau.minecraft.dims.Dims.MOD_ID;

/**
 * Information about a dimension. Also used to create the world instances.
 *
 * @since 0.0.1
 */
final class DimInfo{
    private static final DimRegistry registry = DimRegistry.getInstance();
    
    private final UUID uuid;
    private final String name;
    private final List<DimAttribute> attributes;
    private final Path saveLocation;
    private final RegistryKey<World> registryKey;
    private final DimensionType dimensionType;
    private final RegistryKey<DimensionType> dimensionTypeKey;
    
    private BiomeSource biomeSource;
    private DimensionWorldProperties worldProps;
    
    DimInfo(MinecraftServer server, Path root, NbtCompound tag){
        uuid = tag.getUuid("uuid");
        name = tag.getString("name");
        
        var attributes = new ArrayList<DimAttribute>();
        var attributeList = tag.getList("attributes", NbtType.COMPOUND);
        for(var element : attributeList){
            var attributeTag = (NbtCompound)element;
            var type = DimAttributeType.get(new Identifier(attributeTag.getString("type")))
                .orElseThrow(()->new RuntimeException("Unknown type: " + attributeTag.getString("type")));
            attributes.add(registry.getAttribute(type, new Identifier(attributeTag.getString("attribute")))
                .orElseThrow(()->new RuntimeException("Unknown attribute: " + attributeTag.getString("attribute"))));
        }
        this.attributes = Collections.unmodifiableList(attributes);
        
        saveLocation = root.resolve(tag.getString("save")).toAbsolutePath();
        if(!saveLocation.startsWith(root)){
            throw new RuntimeException("Path traversal!");
        }
        
        //TODO Make this NBT based
        parseAttributes(server, attributes);
        
        worldProps = DimensionWorldProperties.fromNbt(
            tag.getCompound("props"),
            (ServerWorldProperties)server.getOverworld().getLevelProperties(),
            name,
            server.getGameRules()
        );
        
        registryKey = RegistryKey.of(Registry.WORLD_KEY, new Identifier(MOD_ID, getName()));
        dimensionTypeKey = RegistryKey.of(Registry.DIMENSION_TYPE_KEY, new Identifier(MOD_ID, getName()));
        // create(OptionalLong.empty(), true, false, false, true, 1.0D, false, false, true, false, true, 0, 256, 256, HorizontalVoronoiBiomeAccessType.INSTANCE, BlockTags.INFINIBURN_OVERWORLD.getId(), OVERWORLD_ID, 0.0F);
        dimensionType = createDimType(server);
    }
    
    public DimInfo(MinecraftServer server, UUID uuid, String name, Path savePath, List<DimAttribute> attributes){
        this.uuid = uuid;
        this.name = name;
        parseAttributes(server, attributes);
        this.attributes = List.copyOf(attributes);
        this.saveLocation = savePath.resolve("dims").resolve(uuid.toString());
        registryKey = RegistryKey.of(Registry.WORLD_KEY, new Identifier(MOD_ID, getName()));
        dimensionTypeKey = RegistryKey.of(Registry.DIMENSION_TYPE_KEY, new Identifier(MOD_ID, getName()));
        dimensionType = createDimType(server);
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
            if(!(currentAttribute instanceof ControllerDimAttribute)){
                garbage.add(currentAttribute);
                continue;
            }
            // If the attribute is a controller and was already seen, it is also garbage
            var type = currentAttribute.getType();
            if(attributeMap.containsKey(type)){
                garbage.add(currentAttribute);
                continue;
            }
            
            // Now we are getting somewhere, we have a controller
            var controller = (ControllerDimAttribute)currentAttribute;
            
            var appliedAttributes = new ArrayList<DimAttribute>();
            appliedAttributes.add(controller); // Not a bug, required for parsing later
            
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
        
        // And create the dimension stuff
        biomeSource = createBiomeSource(attributeMap.get(DimAttributeType.BIOME_CONTROLLER));
        worldProps = createWorldProperties(server, attributeMap);
    }
    
    /**
     * Creates dimension props based on provided attributes.
     *
     * @param server The server instance
     * @param attributeMap The read attributes from parseAttributes
     * @return The dimension props
     */
    private DimensionWorldProperties createWorldProperties(MinecraftServer server, Map<DimAttributeType, List<DimAttribute>> attributeMap){
        // ServerWorldProperties overworldProps, String name, WeatherController weatherController, DimensionGameRules gameRules
        
        var weatherController = Optional.ofNullable(attributeMap.get(DimAttributeType.WEATHER))
            .map((attributes)->((WeatherDimAttribute)attributes.get(0)).getWeather())
            //TODO Make this the world random you lazy idiot
            .map((type)->WeatherController.create(new Random(), type))
            .orElseGet(()->WeatherController.createDefault(new Random()));
        
        var gameRules = new DimensionGameRules(server.getGameRules());
        
        return new DimensionWorldProperties(
            (ServerWorldProperties)server.getOverworld().getLevelProperties(),
            name,
            weatherController,
            gameRules
        );
    }
    
    /**
     * Creates the biome source for a dimension.
     *
     * @param attributes The attributes to parse
     * @return The biome source from the attributes
     */
    private BiomeSource createBiomeSource(@Nullable List<DimAttribute> attributes){
        // Well we need to make everything up if not supplied
        if(attributes == null){
            attributes = new ArrayList<>();
            // Pick a random controller
            BiomeControllerDimAttribute controller = registry.getRandomAttribute(DimAttributeType.BIOME_CONTROLLER);
            attributes.add(controller);
        }
        if(attributes.size() == 1){
            // TODO Make controller attributes able to provide reasonable defaults
            switch(((BiomeControllerDimAttribute)attributes.get(0)).getController()){
                case SINGLE -> attributes.add(registry.getRandomAttribute(DimAttributeType.BIOME));
                case CHECKERBOARD -> {
                    attributes.add(registry.getRandomAttribute(DimAttributeType.BIOME));
                    attributes.add(registry.getRandomAttribute(DimAttributeType.BIOME));
                }
            }
        }
        
        var controller = (BiomeControllerDimAttribute)attributes.remove(0);
        var biomeRange = controller.getController().getBiomeCountRange();
        // Get the biomes the attributes want
        var biomeList = attributes.stream()
            .map((attribute)->(BiomeDimAttribute)attribute)
            .map(BiomeDimAttribute::getBiome)
            .collect(Collectors.toList());
        
        // TODO Penalize removing and adding biomes at this stage
        // Make sure their are just the right amount of biomes
        while(biomeRange.isOver(biomeList.size())){
            biomeList.add(registry.<BiomeDimAttribute>getRandomAttribute(DimAttributeType.BIOME).getBiome());
        }
        while(biomeRange.isUnder(biomeList.size())){
            biomeList.remove(biomeList.size() - 1);
        }
        
        return switch(controller.getController()){
            case SINGLE -> new FixedBiomeSource(biomeList.get(0));
            // Thanks Mojank
            case CHECKERBOARD -> new CheckerboardBiomeSource(
                biomeList.stream().map((biome)->(Supplier<Biome>)()->biome).collect(Collectors.toList()),
                2
            );
        };
    }
    
    /**
     * Creates a custom dimension type.
     *
     * TODO Make properties for hard coded values
     *
     * @param server The server instance
     * @return The new dimension type
     */
    private DimensionType createDimType(MinecraftServer server){
        var type = DimensionType.create(
            OptionalLong.empty(),
            true,
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
     * Writes this dim info to an NBT tag for serialization.
     *
     * @param root The root path of the save data
     * @return The compound tag
     */
    NbtCompound toNbt(Path root){
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
        
        tag.putString("save", root.relativize(saveLocation).toString());
        
        tag.put("props", worldProps.toNbt());
        
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
        
        return new ServerWorld(
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
