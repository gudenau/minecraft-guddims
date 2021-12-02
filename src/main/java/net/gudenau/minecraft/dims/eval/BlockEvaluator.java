package net.gudenau.minecraft.dims.eval;

import it.unimi.dsi.fastutil.objects.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import net.gudenau.minecraft.dims.accessor.LevelStorage$SessionAccessor;
import net.gudenau.minecraft.dims.util.MiscStuff;
import net.gudenau.minecraft.dims.util.UnsafeHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.*;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.*;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.SessionLock;
import net.minecraft.world.timer.Timer;
import org.jetbrains.annotations.Nullable;

import static net.gudenau.minecraft.dims.Dims.MOD_ID;

public class BlockEvaluator{
    private static final AtomicInteger THREAD_ID = new AtomicInteger();
    private static final ExecutorService THREAD_POOL = new ForkJoinPool(
        Runtime.getRuntime().availableProcessors(),
        (pool)->{
            var thread = new ForkJoinWorkerThread(pool){};
            thread.setDaemon(true);
            thread.setPriority(Thread.NORM_PRIORITY - 2);
            thread.setName("Evaluator Thread " + THREAD_ID.getAndIncrement());
            return thread;
        },
        (thread, exception)->{
            System.err.printf(
                """
                Thread %s (%s) has encountered an unexpected exception.
                """,
                thread.getName(), thread.getThreadGroup().getName()
            );
            
            var crashReport = CrashReport.create(exception, "Error in dims world evaluation");
            crashReport.addElement("threadInfo").add("name", thread.getName()).add("group", thread.getThreadGroup().getName());
            throw new CrashException(crashReport);
        },
        false
    );
    private static volatile boolean evaluating;
    
    public static void evaluateServer(MinecraftServer server){
        var path = MiscStuff.getSavePath(server).resolve("evaluation.nbt.gz");
        if(Files.exists(path)){
            evaluating = false;
            loadEvaluation();
        }else{
            evaluating = true;
            startEvaluation(server);
        }
    }
    
    private static void startEvaluation(MinecraftServer server){
        THREAD_POOL.execute(()->{
            // TODO This takes 10 minutes on my machine, that is too long.
            Path temp;
            try{
                temp = Files.createTempDirectory(MOD_ID);
            }catch(IOException e){
                throw new RuntimeException("Failed to create temp dir for world evaluation", e);
            }
            try{
                var registryManager = server.getRegistryManager();
                var biomeRegistry = registryManager.getMutable(Registry.BIOME_KEY);
    
                try{
                    long start = System.nanoTime();
                    var tempId = new AtomicInteger();
                    var futures = biomeRegistry.stream().map((biome)->CompletableFuture.supplyAsync(()->{
                        var seed = 0xDEADBEEFCAFEBABEL; //FIXME
                        
                        var chunkGenerator = new NoiseChunkGenerator(
                            registryManager.get(Registry.NOISE_WORLDGEN),
                            new FixedBiomeSource(()->biome),
                            seed,
                            ()->registryManager.get(Registry.CHUNK_GENERATOR_SETTINGS_KEY).getOrThrow(switch(biome.getCategory()){
                                case NETHER -> ChunkGeneratorSettings.NETHER;
                                case THEEND -> ChunkGeneratorSettings.END;
                                default -> ChunkGeneratorSettings.OVERWORLD;
                            })
                        );
                        
                        var session = UnsafeHelper.allocateInstance(LevelStorage.Session.class);
                        Path path;
                        try{
                            var tempName = "temp" + tempId.getAndIncrement();
                            if(!tempName.equals("temp0")){
                                return new Object2LongArrayMap<Block>();
                            }
                            path = temp.resolve(tempName);
                            MiscStuff.delete(path);
                            var accessor = (LevelStorage$SessionAccessor)session;
                            accessor.setDirectoryName(tempName);
                            accessor.setDirectory(path);
                            accessor.setLock(SessionLock.create(accessor.getDirectory()));
                        }catch(IOException e){
                            throw new RuntimeException("Failed to evaluate biome " + biomeRegistry.getId(biome), e);
                        }
                        try{
                            // registryManager.get(Registry.DIMENSION_TYPE_KEY).getOrThrow(DimensionType.OVERWORLD_REGISTRY_KEY);
                            var generationWorld = new ServerWorld(
                                server,
                                THREAD_POOL,
                                session,
                                new DummyWorldProps(),
                                RegistryKey.of(Registry.WORLD_KEY, new Identifier(MOD_ID, "temp")),
                                registryManager.get(Registry.DIMENSION_TYPE_KEY).getOrThrow(DimensionType.OVERWORLD_REGISTRY_KEY),
                                new WorldGenerationProgressListener(){
                                    @Override public void start(ChunkPos spawnPos){}
                                    @Override public void setChunkStatus(ChunkPos pos, @Nullable ChunkStatus status){}
                                    @Override public void start(){}
                                    @Override public void stop(){}
                                },
                                chunkGenerator,
                                false,
                                seed,
                                List.of(),
                                false
                            );
    
                            Object2LongMap<Block> blockMap = new Object2LongOpenHashMap<>();
                            for(int chunkY = -8; chunkY < 8; chunkY++){
                                for(int chunkX = -8; chunkX < 8; chunkX++){
                                    Chunk chunk = generationWorld.getChunk(chunkX, chunkY);
            
                                    for(var section : chunk.getSectionArray()){
                                        if(section == null || section.isEmpty()){
                                            continue;
                                        }
                                        section.getBlockStateContainer().count((state, count)->blockMap.computeLong(
                                            state.getBlock(),
                                            (key, storedCount)->storedCount == null ? count : storedCount + count
                                        ));
                                    }
                                }
                            }
    
                            try{
                                session.close();
                            }catch(IOException ignored){}
    
                            return blockMap;
                        }finally{
                            try{
                                MiscStuff.delete(path);
                            }catch(IOException ignored){}
                        }
                    })).collect(Collectors.toUnmodifiableSet());
                    
                    Object2LongMap<Block> blockMap = new Object2LongArrayMap<>();
                    for(var future : futures){
                        var result = future.get();
                        result.forEach((block, count)->
                            blockMap.computeLong(block, (key, storedCount)->storedCount == null ? count : storedCount + count)
                        );
                    }
                    
                    System.out.printf("Took %dns\n", System.nanoTime() - start);
                    blockMap.removeLong(Blocks.AIR);
    
                    blockMap.forEach((block, count)->System.out.printf("%s: %d\n", Registry.BLOCK.getId(block), count));
    
                    System.out.println("Ding!");
                }catch(Throwable t){
                    t.printStackTrace();
                    System.exit(1);
                }
            }finally{
                try{
                    try(var stream = Files.walk(temp)){
                        stream.filter(Files::isRegularFile)
                            .forEach((file)->{
                                try{
                                    Files.delete(file);
                                }catch(IOException ignored){}
                            });
                    }
                    try(var stream = Files.walk(temp)){
                        stream.filter(Files::isDirectory)
                            .forEach((file)->{
                                try{
                                    Files.delete(file);
                                }catch(IOException ignored){}
                            });
                    }
                }catch(IOException ignored){}
                evaluating = false;
                server.execute(()->{
                    for(var player : server.getPlayerManager().getPlayerList()){
                        player.sendMessage(new TranslatableText("chat.gud_dims.evaluating.done"), false);
                    }
                });
            }
        });
    }
    
    private static LevelStorage.Session createDummySession(Path savePath) throws IOException{
        var session = UnsafeHelper.allocateInstance(LevelStorage.Session.class);
        var accessor = (LevelStorage$SessionAccessor)session;
        accessor.setDirectoryName(savePath.getFileName().toString());
        accessor.setDirectory(savePath);
        accessor.setLock(SessionLock.create(savePath));
        return session;
    }
    
    private static void loadEvaluation(){
    
    }
    
    public static void clear(){
    
    }
    
    public static boolean isEvaluating(){
        return evaluating;
    }
    
    private static final class DummyWorldProps implements ServerWorldProperties{
        @Override public String getLevelName(){
            return "dummy";
        }
        @Override public void setThundering(boolean thundering){}
        @Override public int getRainTime(){
            return 0;
        }
        @Override public void setRainTime(int rainTime){}
        @Override public void setThunderTime(int thunderTime){}
        @Override public int getThunderTime(){
            return 0;
        }
        @Override public int getClearWeatherTime(){
            return 0;
        }
        @Override public void setClearWeatherTime(int clearWeatherTime){}
        @Override public int getWanderingTraderSpawnDelay(){
            return 0;
        }
        @Override public void setWanderingTraderSpawnDelay(int wanderingTraderSpawnDelay){}
        @Override public int getWanderingTraderSpawnChance(){
            return 0;
        }
        @Override public void setWanderingTraderSpawnChance(int wanderingTraderSpawnChance){}
        @Override public @Nullable UUID getWanderingTraderId(){
            return null;
        }
        @Override public void setWanderingTraderId(UUID uuid){}
        @Override public GameMode getGameMode(){
            return GameMode.SPECTATOR;
        }
        @Override public void setWorldBorder(WorldBorder.Properties properties){}
        @Override public WorldBorder.Properties getWorldBorder(){
            return WorldBorder.DEFAULT_BORDER;
        }
        @Override public boolean isInitialized(){
            return true;
        }
        @Override public void setInitialized(boolean initialized){}
        @Override public boolean areCommandsAllowed(){
            return false;
        }
        @Override public void setGameMode(GameMode gameMode){}
        @Override public Timer<MinecraftServer> getScheduledEvents(){
            return null;
        }
        @Override public void setTime(long time){}
        @Override public void setTimeOfDay(long timeOfDay){}
        @Override public void setSpawnX(int spawnX){}
        @Override public void setSpawnY(int spawnY){}
        @Override public void setSpawnZ(int spawnZ){}
        @Override public void setSpawnAngle(float angle){}
        @Override public int getSpawnX(){
            return 0;
        }
        @Override public int getSpawnY(){
            return -100000;
        }
        @Override public int getSpawnZ(){
            return 0;
        }
        @Override public float getSpawnAngle(){
            return 0;
        }
        @Override public long getTime(){
            return 0;
        }
        @Override public long getTimeOfDay(){
            return 0;
        }
        @Override public boolean isThundering(){
            return false;
        }
        @Override public boolean isRaining(){
            return false;
        }
        @Override public void setRaining(boolean raining){}
        @Override public boolean isHardcore(){
            return false;
        }
        @Override public GameRules getGameRules(){
            return null;
        }
        @Override public Difficulty getDifficulty(){
            return Difficulty.PEACEFUL;
        }
        @Override public boolean isDifficultyLocked(){
            return true;
        }
    }
}
