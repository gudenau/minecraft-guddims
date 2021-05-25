package net.gudenau.minecraft.dims.api.v0.attribute;

import net.minecraft.block.Block;

/**
 * An attribute that represents a single block.
 *
 * @since 0.0.1
 */
public interface BlockDimAttribute extends DimAttribute{
    /**
     * Gets the block associated with this attribute.
     *
     * @return The associated block
     */
    Block getBlock();
    
    @Override
    default DimAttributeType getType(){
        return DimAttributeType.BLOCK;
    }
}
