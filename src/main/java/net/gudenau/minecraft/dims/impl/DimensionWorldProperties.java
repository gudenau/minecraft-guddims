package net.gudenau.minecraft.dims.impl;

import gudDims.extensions.net.minecraft.world.border.WorldBorder.Properties.WorldBorder$PropertiesExtension;
import java.util.Random;
import java.util.UUID;
import net.fabricmc.fabric.api.util.NbtType;
import net.gudenau.minecraft.dims.impl.weather.WeatherController;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.*;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.timer.Timer;
import net.minecraft.world.timer.TimerCallbackSerializer;
import org.jetbrains.annotations.Nullable;

public final class DimensionWorldProperties implements ServerWorldProperties{
    private final ServerWorldProperties overworldProps;
    private final String name;
    private final WeatherController weatherController;
    private final DimensionGameRules gameRules;
    private WorldBorder.Properties worldBorder;
    private final Timer<MinecraftServer> timer;
    
    private long time;
    private long timeOfDay;
    
    private int wanderingTraderDelay;
    private int wanderingTraderChance;
    private UUID wanderingTraderId;
    
    private int spawnX;
    private int spawnY;
    private int spawnZ;
    private float spawnAngle;
    
    DimensionWorldProperties(ServerWorldProperties overworldProps, String name, WeatherController weatherController, DimensionGameRules gameRules){
        this(
            overworldProps, name, weatherController, gameRules,
            WorldBorder.DEFAULT_BORDER,
            new Timer<>(TimerCallbackSerializer.INSTANCE)
        );
    }
    
    private DimensionWorldProperties(ServerWorldProperties overworldProps, String name, WeatherController weatherController, DimensionGameRules gameRules, WorldBorder.Properties worldBorder, Timer<MinecraftServer> timer){
        this.overworldProps = overworldProps;
        this.name = name;
        this.weatherController = weatherController;
        this.gameRules = gameRules;
        this.worldBorder = worldBorder;
        this.timer = timer;
    }
    
    static DimensionWorldProperties fromNbt(NbtCompound compound, ServerWorldProperties overworldProps, String name, GameRules parentRules){
        // new Timer<>(TimerCallbackSerializer.INSTANCE)
        var props = new DimensionWorldProperties(
            overworldProps,
            name,
            WeatherController.fromNbt(new Random() /* TODO Make this the world random you lazy idiot */, compound.getCompound("weather")),
            DimensionGameRules.fromNbt(compound.getCompound("rules"), parentRules),
            // Why?
            WorldBorder$PropertiesExtension.fromNbt(compound.getCompound("border")),
            Timer.fromNbt(compound.getList("timer", NbtType.COMPOUND), TimerCallbackSerializer.INSTANCE)
        );
    
        props.setTime(compound.getLong("time"));
        props.setTimeOfDay(compound.getLong("timeOfDay"));
        props.setWanderingTraderSpawnDelay(compound.getInt("traderDelay"));
        props.setWanderingTraderSpawnChance(compound.getInt("traderChance"));
        if(compound.contains("traderId")){
            props.setWanderingTraderId(compound.getUuid("traderId"));
        }
        props.setSpawnX(compound.getInt("spawnX"));
        props.setSpawnY(compound.getInt("spawnY"));
        props.setSpawnZ(compound.getInt("spawnZ"));
        props.setSpawnAngle(compound.getFloat("spawnAngle"));
        
        return props;
    }
    
    NbtCompound toNbt(){
        var compound = new NbtCompound();
        compound.put("weather", weatherController.toNbt());
        compound.put("rules", gameRules.toNbt());
        compound.put("border", /* Why */ WorldBorder$PropertiesExtension.toNbt(worldBorder));
        compound.put("timer", timer.toNbt());
        compound.putLong("time", time);
        compound.putLong("timeOfDay", timeOfDay);
        compound.putInt("traderDelay", wanderingTraderDelay);
        compound.putInt("traderChance", wanderingTraderChance);
        if(wanderingTraderId != null){
            compound.putUuid("traderId", wanderingTraderId);
        }
        compound.putInt("spawnX", spawnX);
        compound.putInt("spawnY", spawnY);
        compound.putInt("spawnZ", spawnZ);
        compound.putFloat("spawnAngle", spawnAngle);
        return compound;
    }
    
    void tick(){
        time++;
        timeOfDay++;
        weatherController.tick();
    }
    
    @Override
    public String getLevelName(){
        return name;
    }
    
    @Override
    public void setThundering(boolean thundering){
        weatherController.setThundering(thundering);
    }
    
    @Override
    public int getRainTime(){
        return weatherController.getRainTime();
    }
    
    @Override
    public void setRainTime(int rainTime){
        weatherController.setRainTime(rainTime);
    }
    
    @Override
    public void setThunderTime(int thunderTime){
        weatherController.setThunderTime(thunderTime);
    }
    
    @Override
    public int getThunderTime(){
        return weatherController.getThunderTime();
    }
    
    @Override
    public int getClearWeatherTime(){
        return weatherController.getClearTime();
    }
    
    @Override
    public void setClearWeatherTime(int clearWeatherTime){
        weatherController.setClearTime(clearWeatherTime);
    }
    
    @Override
    public int getWanderingTraderSpawnDelay(){
        return wanderingTraderDelay;
    }
    
    @Override
    public void setWanderingTraderSpawnDelay(int wanderingTraderSpawnDelay){
        wanderingTraderDelay = wanderingTraderSpawnDelay;
    }
    
    @Override
    public int getWanderingTraderSpawnChance(){
        return wanderingTraderChance;
    }
    
    @Override
    public void setWanderingTraderSpawnChance(int wanderingTraderSpawnChance){
        wanderingTraderChance = wanderingTraderSpawnChance;
    }
    
    @Nullable
    @Override
    public UUID getWanderingTraderId(){
        return wanderingTraderId;
    }
    
    @Override
    public void setWanderingTraderId(UUID uuid){
        wanderingTraderId = uuid;
    }
    
    @Override
    public GameMode getGameMode(){
        return overworldProps.getGameMode();
    }
    
    @Override
    public void setWorldBorder(WorldBorder.Properties properties){
        worldBorder = properties;
    }
    
    @Override
    public WorldBorder.Properties getWorldBorder(){
        return worldBorder;
    }
    
    @Override
    public boolean isInitialized(){
        return true;
    }
    
    @Override
    public void setInitialized(boolean initialized){
    
    }
    
    @Override
    public boolean areCommandsAllowed(){
        return overworldProps.areCommandsAllowed();
    }
    
    @Override
    public void setGameMode(GameMode gameMode){
    
    }
    
    @Override
    public Timer<MinecraftServer> getScheduledEvents(){
        return timer;
    }
    
    @Override
    public void setTime(long time){
        this.time = time;
    }
    
    @Override
    public void setTimeOfDay(long timeOfDay){
        this.timeOfDay = timeOfDay;
    }
    
    @Override
    public void setSpawnX(int spawnX){
        this.spawnX = spawnX;
    }
    
    @Override
    public void setSpawnY(int spawnY){
        this.spawnY = spawnY;
    }
    
    @Override
    public void setSpawnZ(int spawnZ){
        this.spawnZ = spawnZ;
    }
    
    @Override
    public void setSpawnAngle(float angle){
        spawnAngle = angle;
    }
    
    @Override
    public int getSpawnX(){
        return spawnX;
    }
    
    @Override
    public int getSpawnY(){
        return spawnY;
    }
    
    @Override
    public int getSpawnZ(){
        return spawnZ;
    }
    
    @Override
    public float getSpawnAngle(){
        return spawnAngle;
    }
    
    @Override
    public long getTime(){
        return time;
    }
    
    @Override
    public long getTimeOfDay(){
        return timeOfDay;
    }
    
    @Override
    public boolean isThundering(){
        return weatherController.isThundering();
    }
    
    @Override
    public boolean isRaining(){
        return weatherController.isRaining();
    }
    
    @Override
    public void setRaining(boolean raining){
        weatherController.setRaining(raining);
    }
    
    @Override
    public boolean isHardcore(){
        return overworldProps.isHardcore();
    }
    
    @Override
    public GameRules getGameRules(){
        return gameRules;
    }
    
    @Override
    public Difficulty getDifficulty(){
        return overworldProps.getDifficulty();
    }
    
    @Override
    public boolean isDifficultyLocked(){
        return overworldProps.isDifficultyLocked();
    }
}
