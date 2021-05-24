package net.gudenau.minecraft.dims.api.v0;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.gudenau.minecraft.dims.api.v0.attribute.DimAttribute;
import net.gudenau.minecraft.dims.api.v0.attribute.DimAttributeType;
import net.gudenau.minecraft.dims.impl.DimRegistryImpl;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public interface DimRegistry{
    static DimRegistry getInstance(){
        return DimRegistryImpl.INSTANCE;
    }
    
    <T extends DimAttribute> List<T> getAttributes(DimAttributeType type);
    List<DimAttribute> getAttributes(DimAttributeType... types);
    
    <T extends DimAttribute> Optional<T> getAttribute(DimAttributeType dimAttributeType, Identifier attribute);
    
    Optional<UUID> createDimension(MinecraftServer server, List<DimAttribute> attributes);
    
    Optional<RegistryKey<World>> getDimensionKey(UUID uuid);
    
    <T extends DimAttribute> T getRandomAttribute(DimAttributeType type);
    DimAttribute getRandomAttribute(DimAttributeType... types);
}
