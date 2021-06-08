package net.gudenau.minecraft.dims.impl;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.util.NbtType;
import net.gudenau.minecraft.dims.Dims;
import net.gudenau.minecraft.dims.accessor.LevelStorage$SessionAccessor;
import net.gudenau.minecraft.dims.accessor.MinecraftServerAccessor;
import net.gudenau.minecraft.dims.api.v0.*;
import net.gudenau.minecraft.dims.api.v0.attribute.*;
import net.gudenau.minecraft.dims.api.v0.controller.*;
import net.gudenau.minecraft.dims.impl.attribute.*;
import net.gudenau.minecraft.dims.impl.client.SkyRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.BlockItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

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
    
    private List<DigitDimAttribute> digitDimAttributeList;
    private Map<Identifier, DigitDimAttribute> digitDimAttributeMap;
    
    private List<BooleanDimAttribute> booleanDimAttributeList;
    private Map<Identifier, BooleanDimAttribute> booleanDimAttributeMap;
    
    private final List<WeatherDimAttribute> weatherDimAttributeList = new ArrayList<>();
    private final Map<Identifier, WeatherDimAttribute> weatherDimAttributeMap = new HashMap<>();
    
    private final List<SkylightDimAttribute> skylightDimAttributeList = new ArrayList<>();
    private final Map<Identifier, SkylightDimAttribute> skylightDimAttributeMap = new HashMap<>();
    
    private final List<CelestialDimAttribute> celestialDimAttributeList = new ArrayList<>();
    private final Map<Identifier, CelestialDimAttribute> celestialDimAttributeMap = new HashMap<>();
    
    private List<CelestialPropertyDimAttribute> celestialPropertyDimAttributeList;
    private Map<Identifier, CelestialPropertyDimAttribute> celestialPropertyDimAttributeMap;
    
    private final Set<DimInfo> pendingDims = new HashSet<>();
    
    @SuppressWarnings({"unchecked", "RedundantCast"})
    @Override
    public <T extends DimAttribute> List<T> getAttributes(DimAttributeType type){
        ensureInit();
        return (List<T>)switch(type){
            case BLOCK -> blockAttributeList;
            case FLUID -> fluidAttributeList;
            case COLOR -> colorAttributeList;
            case BIOME -> biomeAttributeList;
            case BIOME_CONTROLLER -> Collections.unmodifiableList(biomeControllerAttributeList);
            case DIGIT -> digitDimAttributeList;
            case BOOLEAN -> booleanDimAttributeList;
            case WEATHER -> Collections.unmodifiableList(weatherDimAttributeList);
            case SKYLIGHT -> Collections.unmodifiableList(skylightDimAttributeList);
            case CELESTIAL -> Collections.unmodifiableList(celestialDimAttributeList);
            case CELESTIAL_PROPERTY -> celestialPropertyDimAttributeList;
        };
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
            case DIGIT -> digitDimAttributeMap;
            case BOOLEAN -> booleanDimAttributeMap;
            case WEATHER -> Collections.unmodifiableMap(weatherDimAttributeMap);
            case SKYLIGHT -> Collections.unmodifiableMap(skylightDimAttributeMap);
            case CELESTIAL -> Collections.unmodifiableMap(celestialDimAttributeMap);
            case CELESTIAL_PROPERTY -> celestialPropertyDimAttributeMap;
        }).get(attribute));
    }
    
    @Override
    public Optional<UUID> createDimension(MinecraftServer server, List<DimAttribute> attributes){
        UUID uuid = UUID.randomUUID();
        while(dimensions.containsKey(uuid)){
            uuid = UUID.randomUUID();
        }
        
        var info = new DimInfo(server, uuid, "gud_dims_" + dimensions.size(), attributes);
        
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
                if(weatherDimAttributeMap.putIfAbsent(attribute.getId(), attribute) != null){
                    throw new IllegalStateException("Weather controller " + attribute.getId() + " was already registered");
                }
                weatherDimAttributeList.add(attribute);
            }
            case SKYLIGHT -> {
                var attribute = new SkylightDimAttributeImpl((SkylightDimController)controller);
                if(skylightDimAttributeMap.putIfAbsent(attribute.getId(), attribute) != null){
                    throw new IllegalStateException("Skylight controller " + attribute.getId() + " was already registered");
                }
                skylightDimAttributeList.add(attribute);
            }
            case CELESTIAL -> {
                var attribute = new CelestialDimAttributeImpl((CelestialDimController)controller);
                if(celestialDimAttributeMap.put(attribute.getId(), attribute) != null){
                    throw new IllegalStateException("Celestial controller " + attribute.getId() + " was already registered");
                }
                celestialDimAttributeList.add(attribute);
            }
        }
    }
    
    // RedundantCast is because of a javac bug
    @SuppressWarnings({"unchecked", "RedundantCast"})
    @Override
    public <T> Optional<T> getController(ControllerType type, Identifier id){
        return Optional.ofNullable((T)(Object)switch(type){
            case BIOME -> biomeControllerAttributeMap.get(id);
            case WEATHER -> weatherDimAttributeMap.get(id);
            case SKYLIGHT -> skylightDimAttributeMap.get(id);
            case CELESTIAL -> celestialDimAttributeMap.get(id);
        });
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
    
    private Path getSavePath(MinecraftServer server){
        return ((LevelStorage$SessionAccessor)(((MinecraftServerAccessor)server).getSession())).getDirectory().resolve("gud").resolve("dims");
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
    
        digitDimAttributeList = Stream.of(
                IntStream.range(0, 10).mapToObj(DigitDimAttributeImpl::new),
                Stream.of(new DigitDimAttributeImpl(DigitDimAttribute.DigitType.DECIMAL))
            )
            .flatMap((stream)->stream)
            .sorted(Comparator.comparing(DimAttribute::getId))
            .map((attribute)->(DigitDimAttribute)attribute)
            .toList();
        digitDimAttributeMap = toMap(digitDimAttributeList);
        
        booleanDimAttributeList = List.of(
            new BooleanDimAttributeImpl(false),
            new BooleanDimAttributeImpl(true)
        );
        booleanDimAttributeMap = toMap(booleanDimAttributeList);
        
        celestialPropertyDimAttributeList = Stream.of(CelestialPropertyDimAttribute.Property.values())
            .map(CelestialPropertyDimAttributeImpl::new)
            .sorted(Comparator.comparing(DimAttribute::getId))
            .map((attribute)->(CelestialPropertyDimAttribute)attribute)
            .toList();
        celestialPropertyDimAttributeMap = toMap(celestialPropertyDimAttributeList);
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
