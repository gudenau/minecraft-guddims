package net.gudenau.minecraft.dims.impl.attribute;

import java.util.Locale;
import net.gudenau.minecraft.dims.api.v0.attribute.CelestialPropertyDimAttribute;
import net.minecraft.util.Identifier;

import static net.gudenau.minecraft.dims.Dims.MOD_ID;

/**
 * The implementation of a celestial property dimension attribute.
 *
 * @since 0.0.4
 */
public final class CelestialPropertyDimAttributeImpl implements CelestialPropertyDimAttribute{
    private final Property property;
    private final Identifier id;
    
    public CelestialPropertyDimAttributeImpl(CelestialPropertyDimAttribute.Property property){
        this.property = property;
        id = new Identifier(MOD_ID, property.name().toLowerCase(Locale.ROOT));
    }
    
    @Override
    public Identifier getId(){
        return id;
    }
    
    @Override
    public Property getProperty(){
        return property;
    }
}
