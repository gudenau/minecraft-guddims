package net.gudenau.minecraft.dims.api.v0.attribute;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.Identifier;

import static net.gudenau.minecraft.dims.Dims.MOD_ID;

public enum DimAttributeType{
    BLOCK("block"),
    FLUID("fluid"),
    COLOR("color"),
    BIOME("biome"),
    BIOME_CONTROLLER("biome_controller"),
    DIGIT("digit"),
    BOOLEAN("boolean"),
    WEATHER("weather");
    
    private static final Map<Identifier, DimAttributeType> VALUE_MAP;
    
    static{
        ImmutableMap.Builder<Identifier, DimAttributeType> builder = ImmutableMap.builder();
        for(var value : values()){
            builder.put(value.getId(), value);
        }
        VALUE_MAP = builder.build();
    }
    
    public static Optional<DimAttributeType> get(Identifier identifier){
        return Optional.ofNullable(VALUE_MAP.get(identifier));
    }
    
    private final Identifier id;
    
    DimAttributeType(String name){
        id = new Identifier(MOD_ID, name);
    }
    
    public Identifier getId(){
        return id;
    }
}
