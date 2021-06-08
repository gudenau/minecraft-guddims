package net.gudenau.minecraft.dims.impl.controller.celestial.object;

import java.util.OptionalInt;
import net.gudenau.minecraft.dims.api.v0.client.renderer.CelestialObjectRenderer;
import net.gudenau.minecraft.dims.api.v0.controller.CelestialDimController;
import net.gudenau.minecraft.dims.impl.client.renderer.celestial.MoonObjectRenderer;
import net.gudenau.minecraft.dims.impl.controller.celestial.controller.MoonController;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

/**
 * Represents a moon in the sky of a dimension.
 *
 * @since 0.0.4
 */
public final class MoonObject implements CelestialDimController.CelestialObject{
    /**
     * The time it takes in ticks for the sun to make a full revolution.
     */
    private final int period;
    /**
     * The offset in the period that this sun is. Allows for one sun to follow another for example.
     */
    private final int offset;
    /**
     * The rotation of the moon in the sky on the axis it does not rotate on.
     */
    private final int inclination;
    /**
    * The color to apply to the texture.
     */
    private final int color;
    
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public MoonObject(OptionalInt period, OptionalInt offset, OptionalInt inclination, OptionalInt color){
        this.period = period.orElse(24000);
        this.offset = offset.orElse(12000);
        this.inclination = inclination.orElse(0);
        this.color = color.orElse(0xFFFFFF) & 0x00FFFFFF;
    }
    
    @Override
    public CelestialObjectRenderer createRenderer(){
        return new MoonObjectRenderer(period, offset, inclination, color);
    }
    
    @Override
    public void serialize(PacketByteBuf buffer){
        buffer.writeVarInt(period);
        buffer.writeVarInt(offset);
        buffer.writeVarInt(inclination);
        buffer.writeInt(color);
    }
    
    @Override
    public Identifier getId(){
        return MoonController.ID;
    }
}
