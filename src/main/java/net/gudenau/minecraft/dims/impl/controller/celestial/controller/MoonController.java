package net.gudenau.minecraft.dims.impl.controller.celestial.controller;

import java.util.OptionalInt;
import net.gudenau.minecraft.dims.impl.controller.celestial.object.MoonObject;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import static net.gudenau.minecraft.dims.Dims.MOD_ID;

/**
 * Controls how the default moon attribute behaves.
 *
 * @since 0.0.4
 */
public final class MoonController extends SunController{
    public static final Identifier ID = new Identifier(MOD_ID, "moon");
    
    @Override
    protected CelestialObject create(OptionalInt period, OptionalInt offset, OptionalInt inclination, OptionalInt color){
        return new MoonObject(period, offset, inclination, color);
    }
    
    @Override
    public CelestialObject deserialize(PacketByteBuf buffer){
        return new MoonObject(
            OptionalInt.of(buffer.readVarInt()),
            OptionalInt.of(buffer.readVarInt()),
            OptionalInt.of(buffer.readVarInt()),
            OptionalInt.of(buffer.readInt())
        );
    }
    
    @Override
    public Identifier getId(){
        return ID;
    }
}
