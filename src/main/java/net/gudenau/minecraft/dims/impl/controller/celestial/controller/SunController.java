package net.gudenau.minecraft.dims.impl.controller.celestial.controller;

import java.util.List;
import java.util.OptionalInt;
import net.gudenau.minecraft.dims.api.v0.attribute.*;
import net.gudenau.minecraft.dims.api.v0.controller.CelestialDimController;
import net.gudenau.minecraft.dims.api.v0.util.AttributeParser;
import net.gudenau.minecraft.dims.api.v0.util.collection.ObjectIntPair;
import net.gudenau.minecraft.dims.impl.controller.celestial.object.SunObject;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import static net.gudenau.minecraft.dims.Dims.MOD_ID;

/**
 * Controls how the default sun attribute behaves.
 *
 * @since 0.0.4
 */
public class SunController implements CelestialDimController{
    public static final Identifier ID = new Identifier(MOD_ID, "sun");
    
    @Override
    public ObjectIntPair<CelestialObject> createCelestialObject(List<DimAttribute> attributes){
        // Wooo boy this is a mess, any ideas?
        var parser = AttributeParser.of(attributes);
    
        // These may or may not exist
        var period = OptionalInt.empty();
        var offset = OptionalInt.empty();
        var inclination = OptionalInt.empty();
        var color = OptionalInt.empty();
        
        outer:
        // If it's a number or something weird bail
        while(parser.next() == AttributeParser.TokenType.ATTRIBUTE){
            var attribute = parser.getAttribute();
            var attributeType = attribute.getType();
            if(!(attributeType == DimAttributeType.CELESTIAL_PROPERTY || attributeType == DimAttributeType.COLOR)){
                // Bail if we somehow got something weird
                break;
            }
            
            if(attributeType == DimAttributeType.COLOR){
                if(color.isPresent()){
                    break;
                }else{
                    color = OptionalInt.of(((ColorDimAttribute)attribute).getColorValue());
                    continue;
                }
            }
            
            // Convert the token into the attribute
            // This feels bad
            switch(((CelestialPropertyDimAttribute)attribute).getProperty()){
                case PERIOD -> {
                    if(period.isPresent()){
                        break outer;
                    }else{
                        if(parser.next() == AttributeParser.TokenType.NUMBER){
                            period = OptionalInt.of(parser.getInt());
                        }else{
                            break outer;
                        }
                    }
                }
                case OFFSET -> {
                    if(offset.isPresent()){
                        break outer;
                    }else{
                        if(parser.next() == AttributeParser.TokenType.NUMBER){
                            offset = OptionalInt.of(parser.getInt());
                        }else{
                            break outer;
                        }
                    }
                }
                case INCLINATION -> {
                    if(inclination.isPresent()){
                        break outer;
                    }else{
                        if(parser.next() == AttributeParser.TokenType.NUMBER){
                            inclination = OptionalInt.of(parser.getInt());
                        }else{
                            break outer;
                        }
                    }
                }
                default -> {break outer;}
            }
        }
        // Return the results
        return ObjectIntPair.of(
            create(period, offset, inclination, color),
            // This is not quite right, but off by a few is likely fine
            parser.getRemainingAttributeCount()
        );
    }
    
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected CelestialObject create(OptionalInt period, OptionalInt offset, OptionalInt inclination, OptionalInt color){
        return new SunObject(period, offset, inclination, color);
    }
    
    @Override
    public CelestialObject deserialize(PacketByteBuf buffer){
        return new SunObject(
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
    
    @Override
    public boolean isPropertyValid(DimAttribute attribute){
        var type = attribute.getType();
        if(type == DimAttributeType.DIGIT){
            return ((DigitDimAttribute)attribute).getDigitType() == DigitDimAttribute.DigitType.NUMERIC;
        }else if(type == DimAttributeType.CELESTIAL_PROPERTY){
            var prop = ((CelestialPropertyDimAttribute)attribute).getProperty();
            return prop == CelestialPropertyDimAttribute.Property.PERIOD ||
                   prop == CelestialPropertyDimAttribute.Property.OFFSET ||
                   prop == CelestialPropertyDimAttribute.Property.INCLINATION;
        }else{
            return type == DimAttributeType.COLOR;
        }
    }
}
