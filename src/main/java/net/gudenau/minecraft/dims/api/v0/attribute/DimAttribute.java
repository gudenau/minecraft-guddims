package net.gudenau.minecraft.dims.api.v0.attribute;

import net.gudenau.minecraft.dims.item.DimensionAttributeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * A dimension attribute. The base of all other attribute classes.
 *
 * @since 0.0.1
 */
public interface DimAttribute{
    /**
     * Get the associated identifier for this attribute.
     *
     * Important that these are not dynamic, they are used in serialization.
     *
     * @return The identifier for this attribute
     */
    Identifier getId();
    
    /**
     * Gets the type of this attribute.
     *
     * @return The type of this attribute
     */
    DimAttributeType getType();
    
    /**
     * Gets the item stack for this attribute.
     *
     * Primary use is for creative tabs and the "Dimension Token" tooltip.
     *
     * All this does is delegate to DimensionAttributeItem, please don't override.
     *
     * @return The stack for this attribute
     */
    default ItemStack getStack(){
        return DimensionAttributeItem.getStack(this);
    }
    
    /**
     * Gets the item stack used in tooltips.
     *
     * Examples include block and fluid attributes.
     *
     * If this returns an item that is an instance of DimensionAttributeItem it will not have a tooltip.
     *
     * @return The preview stack
     */
    default ItemStack getPreviewStack(){
        return getStack();
    }
}
