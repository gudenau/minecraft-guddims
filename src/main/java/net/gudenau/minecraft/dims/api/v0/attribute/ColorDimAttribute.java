package net.gudenau.minecraft.dims.api.v0.attribute;

import net.gudenau.minecraft.dims.util.MiscStuff;
import net.minecraft.util.DyeColor;

/**
 * An attribute that represents a dye color.
 *
 * @since 0.0.1
 */
public interface ColorDimAttribute extends DimAttribute{
    /**
     * Gets the dye color associated with this attribute.
     *
     * @return The associated dye color
     */
    DyeColor getColor();
    
    /**
     * Gets the color value associated with this attribute.
     *
     * @return The associated color value
     */
    default int getColorValue(){
        return MiscStuff.getDyeColor(getColor());
    }
    
    @Override
    default DimAttributeType getType(){
        return DimAttributeType.COLOR;
    }
}
