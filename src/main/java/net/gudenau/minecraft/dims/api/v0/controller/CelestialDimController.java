package net.gudenau.minecraft.dims.api.v0.controller;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.gudenau.minecraft.dims.api.v0.attribute.CelestialDimAttribute;
import net.gudenau.minecraft.dims.api.v0.attribute.DimAttribute;
import net.gudenau.minecraft.dims.api.v0.client.renderer.CelestialObjectRenderer;
import net.gudenau.minecraft.dims.api.v0.util.collection.ObjectIntPair;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

/**
 * A controller for a celestial object.
 *
 * @since 0.0.4
 */
public interface CelestialDimController extends DimController<CelestialDimAttribute>{
    /**
     * Creates a celestial object instance from a list of attributes.
     *
     * @param attributes The attributes to parse
     * @return A pair of the object and the garbage attributes
     */
    ObjectIntPair<CelestialObject> createCelestialObject(List<DimAttribute> attributes);
    
    /**
     * Deserialize a celestial object from the server.
     *
     * @param buffer The buffer to read
     * @return The deserialized object
     */
    @Environment(EnvType.CLIENT)
    CelestialObject deserialize(PacketByteBuf buffer);
    
    @Override
    default boolean areDuplicatesAllowed(){
        return true;
    }
    
    @Override
    default ControllerType getType(){
        return ControllerType.CELESTIAL;
    }
    
    /**
     * A celestial object, I.E. sun, moon, stars.
     *
     * @since 0.0.4
     */
    interface CelestialObject{
        /**
         * Create a renderer for this object.
         *
         * @return A renderer
         */
        @Environment(EnvType.CLIENT)
        CelestialObjectRenderer createRenderer();
    
        /**
         * Write this object to a buffer to send to clients.
         *
         * @param buffer The buffer
         */
        void serialize(PacketByteBuf buffer);
    
        /**
         * Gets the identifier for the controller of this object.
         *
         * Important for serialization, make sure this matches up.
         *
         * @return The controller ID
         */
        Identifier getId();
    }
}
