package net.gudenau.minecraft.dims.impl.util;

import java.util.List;
import net.gudenau.minecraft.dims.api.v0.attribute.*;
import net.gudenau.minecraft.dims.api.v0.util.AttributeParser;

/**
 * The implementation for AttributeParser.
 *
 * @since 0.0.4
 */
public final class AttributeParserImpl implements AttributeParser{
    private final List<DimAttribute> attributes;
    private int index;
    private TokenType token = TokenType.EMPTY;
    
    public AttributeParserImpl(List<DimAttribute> attributes){
        this.attributes = attributes;
    }
    
    @Override
    public TokenType next(){
        if(token != TokenType.EMPTY){
            return token;
        }
        
        if(index >= attributes.size()){
            return TokenType.END;
        }
        
        var attribute = attributes.get(index);
        
        token = switch(attribute.getType()){
            case DIGIT -> TokenType.NUMBER;
            case BOOLEAN -> TokenType.BOOLEAN;
            default -> TokenType.ATTRIBUTE;
        };
        return token;
    }
    
    @Override
    public int getInt(){
        if(token != TokenType.NUMBER){
            throw new IllegalStateException("Attempted to get a number when one was not available");
        }
        token = TokenType.EMPTY;
        int number = 0;
        for(; index < attributes.size(); index++){
            var attribute = attributes.get(index);
            if(attribute.getType() != DimAttributeType.DIGIT){
                break;
            }
            var digit = (DigitDimAttribute)attribute;
            if(digit.getDigitType() != DigitDimAttribute.DigitType.NUMERIC){
                break;
            }
            number *= 10;
            number += digit.getValue();
        }
        return number;
    }
    
    @Override
    public float getFloat(){
        if(token != TokenType.NUMBER){
            throw new IllegalStateException("Attempted to get a number when one was not available");
        }
        
        // Get the int component first
        float number = 0;
        for(; index < attributes.size(); index++){
            var attribute = attributes.get(index);
            if(attribute.getType() != DimAttributeType.DIGIT){
                break;
            }
            var digit = (DigitDimAttribute)attribute;
            if(digit.getDigitType() != DigitDimAttribute.DigitType.NUMERIC){
                break;
            }
            number *= 10;
            number += digit.getValue();
        }
        {
            var attribute = attributes.get(index);
            // From the previous check we know it's a decimal point
            if(attribute.getType() == DimAttributeType.DIGIT){
                return number;
            }
        }
        float current = 0.1F;
        for(index++; index < attributes.size(); index++){
            var attribute = attributes.get(index);
            if(attribute.getType() != DimAttributeType.DIGIT){
                break;
            }
            var digit = (DigitDimAttribute)attribute;
            if(digit.getDigitType() != DigitDimAttribute.DigitType.NUMERIC){
                break;
            }
            number += digit.getValue() * current;
            current *= 0.1F;
        }
        
        return number;
    }
    
    @Override
    public boolean getBoolean(){
        if(token != TokenType.BOOLEAN){
            throw new IllegalStateException("Attempted to get a boolean when one was not available");
        }
        token = TokenType.EMPTY;
        return ((BooleanDimAttribute)attributes.get(index++)).getBoolean();
    }
    
    @Override
    public DimAttribute getAttribute(){
        if(token != TokenType.ATTRIBUTE){
            throw new IllegalStateException("Attempted to get an attribute when one was not available");
        }
        token = TokenType.EMPTY;
        return attributes.get(index++);
    }
    
    @Override
    public int getRemainingAttributeCount(){
        return attributes.size() - index;
    }
}
