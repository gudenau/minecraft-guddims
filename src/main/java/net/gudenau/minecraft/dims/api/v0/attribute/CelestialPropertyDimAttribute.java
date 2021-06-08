package net.gudenau.minecraft.dims.api.v0.attribute;

/**
 * A property of a celestial object.
 *
 * @since 0.0.4
 */
public interface CelestialPropertyDimAttribute extends DimAttribute{
    @Override
    default DimAttributeType getType(){
        return DimAttributeType.CELESTIAL_PROPERTY;
    }
    
    /**
     * Gets the celestial property of this attribute.
     *
     * @return The celestial property
     */
    Property getProperty();
    
    // TODO Make this extendable
    enum Property{
        PERIOD,
        OFFSET,
        INCLINATION
    }
}
