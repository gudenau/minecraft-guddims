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
            var type = DimAttributeType.get(attributeTag.getIdentifier("type"))
                .orElseThrow(()->new RuntimeException("Unknown type: " + attributeTag.getIdentifier("type")));
            attributes.add(registry.getAttribute(type, attributeTag.getIdentifier("attribute"))
                .orElseThrow(()->new RuntimeException("Unknown attribute: " + attributeTag.getIdentifier("attribute"))));
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
    
    private void parseAttributes(MinecraftServer server, List<DimAttribute> attributes){
        List<DimAttribute> garbage = new ArrayList<>();
        Map<DimAttributeType, List<DimAttribute>> attributeMap = new Object2ObjectOpenHashMap<>();
        for(int i = 0; i < attributes.size(); i++){
            var currentAttribute = attributes.get(i);
            if(!(currentAttribute instanceof ControllerDimAttribute)){
                garbage.add(currentAttribute);
                continue;
            }
            var type = currentAttribute.getType();
            if(attributeMap.containsKey(type)){
                garbage.add(currentAttribute);
                continue;
            }
            
            var controller = (ControllerDimAttribute)currentAttribute;
            
            var appliedAttributes = new ArrayList<DimAttribute>();
            appliedAttributes.add(controller);
            
            for(i++; i < attributes.size(); i++){
                currentAttribute = attributes.get(i);
                if(controller.isPropertyValid(currentAttribute)){
                    appliedAttributes.add(currentAttribute);
                }else{
                    i--;
                    break;
                }
            }
            attributeMap.put(type, appliedAttributes);
        }
        
        biomeSource = createBiomeSource(attributeMap.get(DimAttributeType.BIOME_CONTROLLER));
        worldProps = createWorldProperties(server, attributeMap);
    }
    
    private DimensionWorldProperties createWorldProperties(MinecraftServer server, Map<DimAttributeType, List<DimAttribute>> attributeMap){
        // ServerWorldProperties overworldProps, String name, WeatherController weatherController, DimensionGameRules gameRules
        
        var weatherController = attributeMap.getOptional(DimAttributeType.WEATHER)
            .map((attributes)->((WeatherDimAttribute)attributes.get(0)).getWeather())
            //TODO Make this the world random you lazy idiot
            .map((type)->WeatherController.create(new Random(), type))
            .orElseGet(()->WeatherController.createDefault(new Random()));
        
        var gameRules = new DimensionGameRules();
        
        return new DimensionWorldProperties(
            (ServerWorldProperties)server.getOverworld().getLevelProperties(),
            name,
            weatherController,
            gameRules
        );
    }
    
    private BiomeSource createBiomeSource(@Nullable List<DimAttribute> attributes){
        if(attributes == null){
            attributes = new ArrayList<>();
            BiomeControllerDimAttribute controller = registry.getRandomAttribute(DimAttributeType.BIOME_CONTROLLER);
            attributes.add(controller);
        }
        if(attributes.size() == 1){
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
        var biomeList = attributes.stream()
            .map((attribute)->(BiomeDimAttribute)attribute)
            .map(BiomeDimAttribute::getBiome)
            .collect(Collectors.toList());
        
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
    
    NbtCompound toNbt(Path root){
        var tag = new NbtCompound();
        
        tag.putUuid("uuid", uuid);
        
        tag.putString("name", name);
        
        var attributeList = new NbtList();
        for(DimAttribute attribute : attributes){
            var compound = new NbtCompound();
            compound.putIdentifier("type", attribute.getType().getId());
            compound.putIdentifier("attribute", attribute.getId());
            attributeList.add(compound);
        }
        tag.put("attributes", attributeList);
        
        tag.putString("save", root.relativize(saveLocation).toString());
        
        tag.put("props", worldProps.toNbt());
        
        return tag;
    }
    
    public UUID getUuid(){
        return uuid;
    }
    
    public String getName(){
        return name;
    }
    
    public ServerWorld createWorld(MinecraftServer server){
        var registryManager = server.getRegistryManager();
    
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
        //ChunkGenerator chunkGenerator = GeneratorOptions.createOverworldGenerator(registryManager.get(Registry.BIOME_KEY), registryManager.get(Registry.CHUNK_GENERATOR_SETTINGS_KEY), seed);
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
    
    public RegistryKey<World> getRegistryKey(){
        return registryKey;
    }
}
