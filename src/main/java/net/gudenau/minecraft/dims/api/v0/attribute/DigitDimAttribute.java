package net.gudenau.minecraft.dims.api.v0.attribute;

import java.util.List;

/**
 * An attribute that represents a single base 10 numerical digit, 0-9
 *
 * @since 0.0.1
 */
public interface DigitDimAttribute extends DimAttribute{
    /**
     * Gets the numeric value associated with this attribute.
     *
     * @return The associated numeric value
     */
    int getValue();
    
    @Override
    default DimAttributeType getType(){
        return DimAttributeType.DIGIT;
    }
    
    /**
     * A helper method to get an integer from a list of attributes.
     *
     * @param attributes The digit attributes to parse
     * @return The parsed integer
     */
    static int getIntValue(List<DigitDimAttribute> attributes){
        int value = 0;
        for(DigitDimAttribute attribute : attributes){
            value *= 10;
            value += attribute.getValue();
        }
        return value;
    }
}
