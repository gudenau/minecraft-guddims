package net.gudenau.minecraft.dims.impl.controller.celestial.controller;

import java.util.List;
import java.util.OptionalInt;
import net.gudenau.minecraft.dims.api.v0.attribute.ColorDimAttribute;
import net.gudenau.minecraft.dims.api.v0.attribute.DimAttribute;
import net.gudenau.minecraft.dims.api.v0.attribute.DimAttributeType;
import net.gudenau.minecraft.dims.api.v0.controller.CelestialDimController;
import net.gudenau.minecraft.dims.api.v0.util.collection.ObjectIntPair;
import net.gudenau.minecraft.dims.impl.controller.celestial.object.EndSkyObject;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import static net.gudenau.minecraft.dims.Dims.MOD_ID;

/**
 * The controller for the End sky object in custom dimensions.
 *
 * @since 0.0.4
 */
public final class EndSkyController implements CelestialDimController{
    public static final Identifier ID = new Identifier(MOD_ID, "end_sky");
    
    @Override
    public ObjectIntPair<CelestialObject> createCelestialObject(List<DimAttribute> attributes){
        var color = OptionalInt.empty();
        if(!attributes.isEmpty()){
            var attribute = attributes.get(0);
            if(attribute.getType() == DimAttributeType.COLOR){
                attributes.remove(0);
                color = OptionalInt.of(((ColorDimAttribute)attribute).getColorValue());
            }
        }
        return ObjectIntPair.of(new EndSkyObject(color), attributes.size());
    }
    
    @Override
    public CelestialObject deserialize(PacketByteBuf buffer){
        return new EndSkyObject(OptionalInt.of(buffer.readInt()));
    }
    
    @Override
    public Identifier getId(){
        return ID;
    }
    
    @Override
    public boolean isPropertyValid(DimAttribute attribute){
        return attribute.getType() == DimAttributeType.COLOR;
    }
}
