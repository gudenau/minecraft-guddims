package net.gudenau.minecraft.dims.impl;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.util.NbtType;
import net.fabricmc.fabric.mixin.gamerule.GameRulesAccessor;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.Nullable;

/**
 * A game rule type that allows for per-dimension definition of game rules.
 *
 * TODO Implement most things in this.
 *
 * @since 0.0.1
 */
public final class DimensionGameRules extends GameRules{
    private static final BooleanRule ALWAYS_FALSE = new BooleanRule(null, false){
        @Override
        public boolean get(){
            return false;
        }
    };
    private static Map<String, Key<?>> RULE_MAP;
    
    private final Map<Key<?>, Rule<?>> overrides = new HashMap<>(Map.of(
        GameRules.DO_WEATHER_CYCLE, ALWAYS_FALSE
    ));
    private final GameRules parent;
    
    public DimensionGameRules(GameRules parent){
        populateRuleMap();
        this.parent = parent;
    }
    
    private static void populateRuleMap(){
        if(RULE_MAP != null){
            return;
        }
    
        synchronized(DimensionGameRules.class){
            if(RULE_MAP != null){
                return;
            }
            
            RULE_MAP = GameRulesAccessor.getRuleTypes().keySet().stream()
                .collect(Collector.of(
                    ImmutableMap::<String, Key<?>>builder,
                    (builder, value)->builder.put(value.getName(), value),
                    (a, b)->a.putAll(b.build()),
                    ImmutableMap.Builder::build
                ));
        }
    }
    
    public static DimensionGameRules fromNbt(NbtCompound tag, GameRules parentRules){
        var rules = new DimensionGameRules(parentRules);
        var list = tag.getList("rules", NbtType.COMPOUND);
        list.stream()
            .map((element)->(NbtCompound)element)
            .forEach((compound)->{
                var key = compound.getString("key");
                var value = switch(compound.getString("type")){
                    case "always_false" -> ALWAYS_FALSE;
                    default -> throw new IllegalStateException("Unknown DimensionGameRules type: " + compound.getString("type"));
                };
                rules.overrides.put(RULE_MAP.get(key), value);
            });
        return rules;
    }
    
    @Override
    public NbtCompound toNbt(){
        var compound = new NbtCompound();
        var list = new NbtList();
        overrides.forEach((key, value)->{
            var tag = new NbtCompound();
            tag.putString("key", key.getName());
            String type;
            if(value == ALWAYS_FALSE){
                type = "always_false";
            }else{
                throw new IllegalStateException("Unknown override: " + value);
            }
            tag.putString("type", type);
            list.add(tag);
        });
        compound.put("rules", list);
        return compound;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Rule<T>> T get(Key<T> key){
        var override = overrides.get(key);
        if(override != null){
            return (T)override;
        }
        
        return parent.get(key);
    }
    
    @Override
    public GameRules copy(){
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void setAllValues(GameRules rules, @Nullable MinecraftServer server){
        throw new UnsupportedOperationException();
    }
}
