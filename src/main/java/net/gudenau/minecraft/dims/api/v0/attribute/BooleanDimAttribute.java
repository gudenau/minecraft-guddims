package net.gudenau.minecraft.dims.api.v0.attribute;

/**
 * An attribute that represents a boolean value.
 *
 * @since 0.0.1
 */
public interface BooleanDimAttribute extends DimAttribute{
    /**
     * Gets the boolean value associated with this attribute.
     *
     * @return The associated boolean
     */
    boolean getBoolean();
    
    @Override
    default DimAttributeType getType(){
        return DimAttributeType.BOOLEAN;
    }
}
