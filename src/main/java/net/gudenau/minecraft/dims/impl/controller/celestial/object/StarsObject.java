package net.gudenau.minecraft.dims.impl.controller.celestial.object;

import java.util.OptionalInt;
import net.gudenau.minecraft.dims.api.v0.client.renderer.CelestialObjectRenderer;
import net.gudenau.minecraft.dims.api.v0.controller.CelestialDimController;
import net.gudenau.minecraft.dims.impl.client.renderer.celestial.StarsRenderer;
import net.gudenau.minecraft.dims.impl.controller.celestial.controller.StarsController;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

/**
 * The stars object for custom dimensions.
 *
 * @since 0.0.4
 */
public final class StarsObject implements CelestialDimController.CelestialObject{
    private final int color;
    
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public StarsObject(OptionalInt color){
        this.color = color.orElse(0xFFFFFF) & 0x00FFFFFF;
    }
    
    @Override
    public CelestialObjectRenderer createRenderer(){
        return new StarsRenderer(color);
    }
    
    @Override
    public void serialize(PacketByteBuf buffer){
        buffer.writeInt(color);
    }
    
    @Override
    public Identifier getId(){
        return StarsController.ID;
    }
}
