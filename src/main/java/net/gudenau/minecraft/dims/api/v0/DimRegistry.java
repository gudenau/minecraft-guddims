package net.gudenau.minecraft.dims.api.v0;

import java.util.*;
import net.gudenau.minecraft.dims.api.v0.attribute.DimAttribute;
import net.gudenau.minecraft.dims.api.v0.attribute.DimAttributeType;
import net.gudenau.minecraft.dims.api.v0.controller.ControllerType;
import net.gudenau.minecraft.dims.api.v0.controller.DimController;
import net.gudenau.minecraft.dims.impl.DimInfo;
import net.gudenau.minecraft.dims.impl.DimRegistryImpl;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A centralized place to interact with this mod. It provides information about dimensions, dimension attributes as well
 * as ways to add more attributes and register dimensions.
 *
 * @since 0.0.1
 */
public interface DimRegistry{
    /**
     * Gets the instance of this registry.
     *
     * @return The instance
     */
    static DimRegistry getInstance(){
        return DimRegistryImpl.INSTANCE;
    }
    
    /**
     * Gets all of the attribute types.
     *
     * @since 0.0.2
     *
     * @return A list of all attribute types
     */
    default List<DimAttributeType> getAttributeTypes(){
        return List.of(DimAttributeType.values());
    }
    
    /**
     * Gets all of the attributes of a given type.
     *
     * @param type The attribute type, IE BLOCK
     * @param <T> The type of attribute, IE BlockDimAttribute
     * @return A list of attributes
     */
    <T extends DimAttribute> List<T> getAttributes(DimAttributeType type);
    
    /**
     * Gets all of the attributes of the given types.
     *
     * @param types The attribute types
     * @return A list of attributes
     */
    List<DimAttribute> getAttributes(DimAttributeType... types);
    
    /**
     * Gets an attribute type from an identifier, or empty if it does not exist.
     *
     * @since 0.0.2
     *
     * @param attributeType The identifier
     * @return The attribute type or empty
     */
    default Optional<DimAttributeType> getAttributeType(Identifier attributeType){
        return DimAttributeType.get(attributeType);
    }
    
    /**
     * Gets a specific dimension attribute, or empty if it does not exist.
     *
     * @param attributeType The type of the attribute, IE BLOCK
     * @param attribute The identifier of the attribute
     * @param <T> The attribute type, IE BlockDimAttribute
     * @return The attribute or empty
     */
    <T extends DimAttribute> Optional<T> getAttribute(DimAttributeType attributeType, Identifier attribute);
    
    /**
     * Gets a specific dimension attribute, or empty if it does not exist.
     *
     * @since 0.0.2
     *
     * @param attributeType The type of the attribute, IE gud_dims:block
     * @param attribute The identifier of the attribute
     * @param <T> The attribute type, IE BlockDimAttribute
     * @return The attribute or empty
     */
    default <T extends DimAttribute> Optional<T> getAttribute(Identifier attributeType, Identifier attribute){
        return getAttributeType(attributeType).flatMap((type)->getAttribute(type, attribute));
    }
    
    /**
     * Attempts to create a new dimension from a list of attributes.
     *
     * If the dimension was created successfully a UUID will be returned, if it failed empty will be returned.
     *
     * @param server An instance of the running server
     * @param attributes The attributes of the new dimension
     * @return The UUID of the dimension, or empty
     */
    default Optional<UUID> createDimension(MinecraftServer server, List<DimAttribute> attributes){
        return createDimension(server, null, attributes);
    }
    
    /**
     * Attempts to create a new dimension from a list of attributes.
     *
     * If the dimension was created successfully a UUID will be returned, if it failed empty will be returned.
     *
     * @param server An instance of the running server
     * @param name The name of the new dimension
     * @param attributes The attributes of the new dimension
     * @return The UUID of the dimension, or empty
     */
    Optional<UUID> createDimension(MinecraftServer server, @Nullable String name, List<DimAttribute> attributes);
    
    /**
     * Gets the registry key for a dimension, or empty if it does not exist.
     *
     * @param uuid The uuid of the dimension
     * @return The registry key or empty
     */
    Optional<RegistryKey<World>> getDimensionKey(UUID uuid);
    
    /**
     * Gets a random attribute of a given type.
     *
     * @param type The attribute type to pull from, IE BLOCK
     * @param <T> The type of the attribute, IE BlockDimAttribute
     * @return The random attribute
     */
    <T extends DimAttribute> T getRandomAttribute(DimAttributeType type);
    
    /**
     * Gets a random attribute from a pool of types.
     *
     * @param types The attribute types to pull from
     * @return The random attribute
     */
    DimAttribute getRandomAttribute(DimAttributeType... types);
    
    /**
     * Registers a collection of dimension controllers.
     *
     * @param controllers The controllers to register
     */
    default void registerControllers(Collection<@NotNull DimController<?>> controllers){
        controllers.forEach(this::registerController);
    }
    
    /**
     * Registers a dimension controller.
     *
     * @param controller The controller to register
     */
    void registerController(DimController<?> controller);
    
    /**
     * Gets a controller attribute, if it exists.
     *
     * @param type The type of controller
     * @param id The id of the controller
     * @param <T> FIXME
     * @return The controller or empty
     *
     * @since 0.0.4
     */
    <T> Optional<T> getController(ControllerType type, Identifier id);
    
    Optional<DimInfo> getDimensionInfo(RegistryKey<World> key);
}
