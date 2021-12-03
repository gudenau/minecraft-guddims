package net.gudenau.minecraft.dims.impl;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.util.NbtType;
import net.gudenau.minecraft.dims.Dims;
import net.gudenau.minecraft.dims.accessor.MinecraftServerAccessor;
import net.gudenau.minecraft.dims.api.v0.DimRegistry;
import net.gudenau.minecraft.dims.api.v0.attribute.*;
import net.gudenau.minecraft.dims.api.v0.controller.*;
import net.gudenau.minecraft.dims.impl.attribute.*;
import net.gudenau.minecraft.dims.impl.client.SkyRegistry;
import net.gudenau.minecraft.dims.impl.controller.feature.FeatureController;
import net.minecraft.block.Blocks;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.BlockItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.SpawnLocating;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.jetbrains.annotations.Nullable;

import static net.gudenau.minecraft.dims.util.MiscStuff.getSavePath;

/**
 * The implementation of the registry.
 *
 * @since 0.0.1
 */
public final class DimRegistryImpl implements DimRegistry{
    public static final DimRegistryImpl INSTANCE = new DimRegistryImpl();
    private static final Random random = new Random(System.nanoTime() ^ 0xDEADBEEFCAFEBABEL);
    
    private boolean initialInit = false;
    
    private final Map<UUID, DimInfo> dimensions = new HashMap<>();
    
    private List<BlockDimAttribute> blockAttributeList;
    private Map<Identifier, BlockDimAttribute> blockAttributeMap;
    
    private List<FluidDimAttribute> fluidAttributeList;
    private Map<Identifier, FluidDimAttribute> fluidAttributeMap;
    
    private List<ColorDimAttribute> colorAttributeList;
    private Map<Identifier, ColorDimAttribute> colorAttributeMap;
    
    private List<BiomeDimAttribute> biomeAttributeList;
    private Map<Identifier, BiomeDimAttribute> biomeAttributeMap;
    
    private final List<BiomeControllerDimAttribute> biomeControllerAttributeList = new ArrayList<>();
    private final Map<Identifier, BiomeControllerDimAttribute> biomeControllerAttributeMap = new HashMap<>();
    
    private List<DigitDimAttribute> digitAttributeList;
    private Map<Identifier, DigitDimAttribute> digitAttributeMap;
    
    private List<BooleanDimAttribute> booleanAttributeList;
    private Map<Identifier, BooleanDimAttribute> booleanAttributeMap;
    
    private final List<WeatherDimAttribute> weatherAttributeList = new ArrayList<>();
    private final Map<Identifier, WeatherDimAttribute> weatherAttributeMap = new HashMap<>();
    
    private final List<SkylightDimAttribute> skylightAttributeList = new ArrayList<>();
    private final Map<Identifier, SkylightDimAttribute> skylightAttributeMap = new HashMap<>();
    
    private final List<CelestialDimAttribute> celestialAttributeList = new ArrayList<>();
    private final Map<Identifier, CelestialDimAttribute> celestialAttributeMap = new HashMap<>();
    
    private List<CelestialPropertyDimAttribute> celestialPropertyAttributeList;
    private Map<Identifier, CelestialPropertyDimAttribute> celestialPropertyAttributeMap;
    
    private List<FeatureDimAttribute> featureAttributeList;
    private Map<Identifier, FeatureDimAttribute> featureAttributeMap;
    
    private final List<FeatureControllerDimAttribute> featureControllerAttributeList = new ArrayList<>();
    private final Map<Identifier, FeatureControllerDimAttribute> featureControllerAttributeMap = new HashMap<>();
    
    private final Set<DimInfo> pendingDims = new HashSet<>();
    
    @SuppressWarnings({"unchecked"})
    @Override
    public <T extends DimAttribute> List<T> getAttributes(DimAttributeType type){
        ensureInit();
        var result = (List<T>)switch(type){
            case BLOCK -> blockAttributeList;
            case FLUID -> fluidAttributeList;
            case COLOR -> colorAttributeList;
            case BIOME -> biomeAttributeList;
            case BIOME_CONTROLLER -> biomeControllerAttributeList;
            case DIGIT -> digitAttributeList;
            case BOOLEAN -> booleanAttributeList;
            case WEATHER -> weatherAttributeList;
            case SKYLIGHT -> skylightAttributeList;
            case CELESTIAL -> celestialAttributeList;
            case CELESTIAL_PROPERTY -> celestialPropertyAttributeList;
            case FEATURE -> featureAttributeList;
            case FEATURE_CONTROLLER -> featureControllerAttributeList;
        };
        return result == null ? List.of() : Collections.unmodifiableList(result);
    }
    
    @Override
    public List<DimAttribute> getAttributes(DimAttributeType... types){
        ensureInit();
        return Stream.of(types)
            .map(this::getAttributes)
            .flatMap(List::stream)
            .distinct()
            .toList();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T extends DimAttribute> Optional<T> getAttribute(DimAttributeType attributeType, Identifier attribute){
        ensureInit();
        return Optional.ofNullable(((Map<Identifier, T>)switch(attributeType){
            case BLOCK -> blockAttributeMap;
            case FLUID -> fluidAttributeMap;
            case COLOR -> colorAttributeMap;
            case BIOME -> biomeAttributeMap;
            case BIOME_CONTROLLER -> Collections.unmodifiableMap(biomeControllerAttributeMap);
            case DIGIT -> digitAttributeMap;
            case BOOLEAN -> booleanAttributeMap;
            case WEATHER -> Collections.unmodifiableMap(weatherAttributeMap);
            case SKYLIGHT -> Collections.unmodifiableMap(skylightAttributeMap);
            case CELESTIAL -> Collections.unmodifiableMap(celestialAttributeMap);
            case CELESTIAL_PROPERTY -> celestialPropertyAttributeMap;
            case FEATURE -> Collections.unmodifiableMap(featureAttributeMap);
            case FEATURE_CONTROLLER -> featureControllerAttributeMap;
        }).get(attribute));
    }
    
    @Override
    public Optional<UUID> createDimension(MinecraftServer server, @Nullable String name, List<DimAttribute> attributes){
        UUID uuid = UUID.randomUUID();
        while(dimensions.containsKey(uuid)){
            uuid = UUID.randomUUID();
        }
        
        //FIXME This is trash.
        int dimensionIndex = 0;
        String dimensionName = name == null ? "gud_dims_0" : name;
        do{
            var lambdaName = dimensionName;
            if(dimensions.values().stream().noneMatch((dim)->dim.getName().equals(lambdaName))){
                break;
            }
            dimensionIndex++;
            dimensionName = name == null ? "gud_dims_" + dimensionIndex : name + "_" + dimensionIndex;
        }while(true);
        
        var info = new DimInfo(server, uuid, dimensionName, true, attributes);
        
        synchronized(pendingDims){
            pendingDims.add(info);
        }
        dimensions.put(info.getUuid(), info);
    
        var buffer = PacketByteBufs.create();
        buffer.writeIdentifier(info.getRegistryKey().getValue());
        
        var playerManager = server.getPlayerManager();
        playerManager.sendToAll(ServerPlayNetworking.createS2CPacket(Dims.Packets.REGISTER_DIM, buffer));
        playerManager.sendToAll(SkyRegistry.createPacket(info));
        
        return Optional.of(info.getUuid());
    }
    
    public void addWorlds(MinecraftServer server){
        synchronized(pendingDims){
            var worlds = ((MinecraftServerAccessor)server).getWorlds();
            for(var info : pendingDims){
                var world = info.createWorld(server);
                pickWorldSpawn(world);
                worlds.put(info.getRegistryKey(), world);
                ServerWorldEvents.LOAD.invoker().onWorldLoad(server, world);
            }
            pendingDims.clear();
        }
    }
    
    @Override
    public Optional<RegistryKey<World>> getDimensionKey(UUID uuid){
        return Optional.ofNullable(dimensions.get(uuid)).map(DimInfo::getRegistryKey);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T extends DimAttribute> T getRandomAttribute(DimAttributeType type){
        ensureInit();
        var attributes = getAttributes(type);
        return (T)attributes.get(random.nextInt(attributes.size()));
    }
    
    @Override
    public DimAttribute getRandomAttribute(DimAttributeType... types){
        ensureInit();
        var attributes = Stream.of(types)
            .map(this::getAttributes)
            .collect(Collectors.toList());
        
        int limit = 0;
        for(var list : attributes){
            limit += list.size();
        }
        
        var index = random.nextInt(limit);
        final var finalIndex = index;
        for(var list : attributes){
            if(index >= list.size()){
                index -= list.size();
            }else{
                return list.get(index);
            }
        }
        
        throw new RuntimeException(String.format(
            "Somehow failed to get an attribute for index %d and types %s",
            finalIndex,
            Stream.of(types).map((type)->type.getId().toString()).collect(Collectors.joining(", "))
        ));
    }
    
    @Override
    public void registerController(DimController<?> controller){
        switch(controller.getType()){
            case BIOME -> {
                var attribute = new BiomeControllerDimAttributeImpl((BiomeDimController)controller);
                if(biomeControllerAttributeMap.putIfAbsent(attribute.getId(), attribute) != null){
                    throw new IllegalStateException("Biome controller " + attribute.getId() + " was already registered");
                }
                biomeControllerAttributeList.add(attribute);
            }
            case WEATHER -> {
                var attribute = new WeatherDimAttributeImpl((WeatherDimController)controller);
                if(weatherAttributeMap.putIfAbsent(attribute.getId(), attribute) != null){
                    throw new IllegalStateException("Weather controller " + attribute.getId() + " was already registered");
                }
                weatherAttributeList.add(attribute);
            }
            case SKYLIGHT -> {
                var attribute = new SkylightDimAttributeImpl((SkylightDimController)controller);
                if(skylightAttributeMap.putIfAbsent(attribute.getId(), attribute) != null){
                    throw new IllegalStateException("Skylight controller " + attribute.getId() + " was already registered");
                }
                skylightAttributeList.add(attribute);
            }
            case CELESTIAL -> {
                var attribute = new CelestialDimAttributeImpl((CelestialDimController)controller);
                if(celestialAttributeMap.put(attribute.getId(), attribute) != null){
                    throw new IllegalStateException("Celestial controller " + attribute.getId() + " was already registered");
                }
                celestialAttributeList.add(attribute);
            }
            case FEATURE -> {
                var attribute = new FeatureControllerDimAttributeImpl((FeatureController)controller);
                if(featureControllerAttributeMap.put(attribute.getId(), attribute) != null){
                    throw new IllegalStateException("Feature controller " + attribute.getId() + " was already registered");
                }
                featureControllerAttributeList.add(attribute);
            }
            default -> throw new RuntimeException("Unimplemented controller type: " + controller.getType().name());
        }
    }
    
    // RedundantCast is because of a javac bug
    @SuppressWarnings({"unchecked", "RedundantCast"})
    @Override
    public <T> Optional<T> getController(ControllerType type, Identifier id){
        return Optional.ofNullable((T)(Object)switch(type){
            case BIOME -> biomeControllerAttributeMap.get(id);
            case WEATHER -> weatherAttributeMap.get(id);
            case SKYLIGHT -> skylightAttributeMap.get(id);
            case CELESTIAL -> celestialAttributeMap.get(id);
            case FEATURE -> featureAttributeMap.get(id);
        });
    }
    
    //TODO Optimize?
    @Override
    public Optional<DimInfo> getDimensionInfo(RegistryKey<World> key){
        return dimensions.values().stream().filter((dimInfo)->dimInfo.getRegistryKey().equals(key)).findAny();
    }
    
    public void init(MinecraftServer server){
        ensureInit();
        ephemeralInit(server);
    }
    
    public void createWorlds(MinecraftServer server){
        var worlds = ((MinecraftServerAccessor)server).getWorlds();
        for(DimInfo dimInfo : dimensions.values()){
            worlds.put(dimInfo.getRegistryKey(), dimInfo.createWorld(server));
        }
    }
    
    private void pickWorldSpawn(ServerWorld world){
        var chunkGenerator = world.getChunkManager().getChunkGenerator();
        var chunkPos = new ChunkPos(chunkGenerator.getMultiNoiseSampler().findBestSpawnPosition());
        var spawnHeight = chunkGenerator.getSpawnHeight(world);
        if(spawnHeight < world.getBottomY()){
            BlockPos blockPos = chunkPos.getStartPos();
            spawnHeight = world.getTopY(Heightmap.Type.WORLD_SURFACE, blockPos.getX() + 8, blockPos.getZ() + 8);
        }
        if(!(world.getLevelProperties() instanceof DimensionWorldProperties worldProperties)){
            return;
        }
        worldProperties.setSpawnPos(chunkPos.getStartPos().add(8, spawnHeight, 8), 0);
        int offsetX = 0;
        int offsetZ = 0;
        int xDelta = 0;
        int zDelta = -1;
        for(int i = 0; i < MathHelper.square(11); i++){
            BlockPos newSpawn;
            if(offsetX >= -5 && offsetX <= 5 && offsetZ >= -5 && offsetZ <= 5){
                newSpawn = SpawnLocating.findServerSpawnPoint(world, new ChunkPos(chunkPos.x + offsetX, chunkPos.z + offsetZ));
                if(newSpawn != null){
                    worldProperties.setSpawnPos(newSpawn, 0.0f);
                    break;
                }
            }
            if(offsetX == offsetZ || offsetX < 0 && offsetX == -offsetZ || offsetX > 0 && offsetX == 1 - offsetZ){
                int blockPos22 = xDelta;
                xDelta = -zDelta;
                zDelta = blockPos22;
            }
            offsetX += xDelta;
            offsetZ += zDelta;
        }
    }
    
    public void loadDimensions(MinecraftServer server){
        var path = getSavePath(server);
        
        var dimPath = path.resolve("dimensions.nbt.gz");
        if(!Files.exists(dimPath)){
            return;
        }
    
        NbtCompound tag;
        try(var stream = Files.newInputStream(dimPath)){
            tag = NbtIo.readCompressed(stream);
        }catch(IOException e){
            throw new RuntimeException("Failed to load extra dimensions", e);
        }
        for(var element : tag.getList("dimensions", NbtType.COMPOUND)){
            var dim = new DimInfo(server, (NbtCompound)element);
            dimensions.put(dim.getUuid(), dim);
        }
    }
    
    private void ephemeralInit(MinecraftServer server){
        var registryManager = server.getRegistryManager();
    
        var biomeRegistry = registryManager.getMutable(Registry.BIOME_KEY);
        biomeAttributeList = biomeRegistry.stream()
            .map((biome)->(BiomeDimAttribute)new BiomeDimAttributeImpl(biome, biomeRegistry.getId(biome)))
            .toList();
        biomeAttributeMap = toMap(biomeAttributeList);
    
        // This is stupid.
        int featureStepCount = GenerationStep.Feature.values().length;
        List<Set<PlacedFeature>> featureSetList = new ArrayList<>();
        for(int i = 0; i < featureStepCount; i++){
            featureSetList.add(new HashSet<>());
        }
        biomeRegistry.stream()
            .map((biome)->biome.getGenerationSettings().getFeatures())
            .forEach((biomeFeatureListList)->{
                for(int i = 0; i < biomeFeatureListList.size(); i++){
                    var biomeFeatureList = biomeFeatureListList.get(i);
                    if(biomeFeatureList == null || biomeFeatureList.isEmpty()){
                        continue;
                    }
                    var featureSet = featureSetList.get(i);
                    biomeFeatureList.stream()
                        .map(Supplier::get)
                        .forEach(featureSet::add);
                }
            });
        
        var featureRegistry = registryManager.getMutable(Registry.PLACED_FEATURE_KEY);
        featureAttributeList = featureRegistry.stream()
            .map((feature)->{
                int featureStep = -1;
                for(int i = 0; i < featureStepCount; i++){
                    if(featureSetList.get(i).contains(feature)){
                        featureStep = i;
                        break;
                    }
                }
                if(featureStep == -1){
                    return null;
                }
                
                return (FeatureDimAttribute)new FeatureDimAttributeImpl(feature, featureStep, featureRegistry.getId(feature));
            })
            .filter(Objects::nonNull)
            .toList();
        featureAttributeMap = toMap(featureAttributeList);
    }
    
    public void saveWorlds(MinecraftServer server){
        var path = getSavePath(server);
    
        var dimPath = path.resolve("dimensions.nbt.gz");
        try{
            Files.createDirectories(dimPath.getParent());
    
            NbtCompound tag = new NbtCompound();
            
            var dims = new NbtList();
            for(var info : dimensions.values()){
                dims.add(info.toNbt());
            }
            tag.put("dimensions", dims);
    
            try(var stream = Files.newOutputStream(dimPath)){
                NbtIo.writeCompressed(tag, stream);
            }
        }catch(IOException e){
            throw new UncheckedIOException("Failed to save dims", e);
        }
    }
    
    public void deinit(MinecraftServer server){
        var worlds = ((MinecraftServerAccessor)server).getWorlds();
        dimensions.values().stream()
            .map(DimInfo::getRegistryKey)
            .forEach(worlds::remove);
        
        dimensions.clear();
        
        biomeAttributeList = null;
        biomeAttributeMap = null;
    }
    
    private void ensureInit(){
        if(initialInit){
            return;
        }
        initialInit = true;
        
        var language = Language.getInstance();
        blockAttributeList = Registry.BLOCK.stream()
            .filter((block)->{
                var item = block.asItem();
                if(item instanceof BlockItem){
                    return ((BlockItem)item).getBlock() != Blocks.AIR;
                }else{
                    return !(item instanceof AirBlockItem);
                }
            })
            .map((block)->new BlockDimAttributeImpl(block, Registry.BLOCK.getId(block)))
            .filter((attribute)->{
                var id = attribute.getId();
                return language.hasTranslation("block." + id.getNamespace() + "." + id.getPath());
            })
            .sorted(Comparator.comparing(DimAttribute::getId))
            .map((attribute)->(BlockDimAttribute)attribute)
            .toList();
        blockAttributeMap = toMap(blockAttributeList);
        
        fluidAttributeList = Registry.FLUID.stream()
            .filter((fluid)->fluid.getDefaultState().isStill())
            .distinct()
            .map((fluid)->new FluidDimAttributeImpl(fluid, Registry.FLUID.getId(fluid)))
            .sorted(Comparator.comparing(DimAttribute::getId))
            .map((attribute)->(FluidDimAttribute)attribute)
            .toList();
        fluidAttributeMap = toMap(fluidAttributeList);
        
        colorAttributeList = Stream.of(DyeColor.values())
            .map((color)->new ColorDimAttributeImpl(color, new Identifier("minecraft", color.getName())))
            .sorted(Comparator.comparing(DimAttribute::getId))
            .map((attribute)->(ColorDimAttribute)attribute)
            .toList();
        colorAttributeMap = toMap(colorAttributeList);
    
        digitAttributeList = Stream.of(
                IntStream.range(0, 10).mapToObj(DigitDimAttributeImpl::new),
                Stream.of(new DigitDimAttributeImpl(DigitDimAttribute.DigitType.DECIMAL))
            )
            .flatMap((stream)->stream)
            .sorted(Comparator.comparing(DimAttribute::getId))
            .map((attribute)->(DigitDimAttribute)attribute)
            .toList();
        digitAttributeMap = toMap(digitAttributeList);
        
        booleanAttributeList = List.of(
            new BooleanDimAttributeImpl(false),
            new BooleanDimAttributeImpl(true)
        );
        booleanAttributeMap = toMap(booleanAttributeList);
        
        celestialPropertyAttributeList = Stream.of(CelestialPropertyDimAttribute.Property.values())
            .map(CelestialPropertyDimAttributeImpl::new)
            .sorted(Comparator.comparing(DimAttribute::getId))
            .map((attribute)->(CelestialPropertyDimAttribute)attribute)
            .toList();
        celestialPropertyAttributeMap = toMap(celestialPropertyAttributeList);
    }
    
    private static <T extends DimAttribute> Map<Identifier, T> toMap(List<T> attributes){
        ImmutableMap.Builder<Identifier, T> builder = ImmutableMap.builder();
        attributes.stream()
            .map((attribute)->Map.entry(attribute.getId(), attribute))
            .forEach(builder::put);
        return builder.build();
    }
    
    public Collection<DimInfo> getDimensions(){
        return Collections.unmodifiableCollection(dimensions.values());
    }
}
