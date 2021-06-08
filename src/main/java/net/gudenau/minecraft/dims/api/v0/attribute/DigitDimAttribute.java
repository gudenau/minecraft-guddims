package net.gudenau.minecraft.dims.api.v0.attribute;


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
    
    /**
     * Gets what type of digit this attribute is.
     *
     * @since 0.0.4
     *
     * @return The digit type.
     */
    DigitType getDigitType();
    
    @Override
    default DimAttributeType getType(){
        return DimAttributeType.DIGIT;
    }
    
    /**
     * The types of digit attributes.
     *
     * @since 0.0.4
     */
    enum DigitType{
        NUMERIC, DECIMAL
    }
}
