package net.gudenau.minecraft.dims.api.v0.attribute;

import net.minecraft.block.Block;

public interface BlockDimAttribute extends DimAttribute{
    Block getBlock();
    
    @Override
    default DimAttributeType getType(){
        return DimAttributeType.BLOCK;
    }
}
