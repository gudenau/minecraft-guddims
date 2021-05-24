package net.gudenau.minecraft.dims.impl.attribute;

import net.gudenau.minecraft.dims.api.v0.attribute.BlockDimAttribute;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public final class BlockDimAttributeImpl implements BlockDimAttribute{
    private final Block block;
    private final Identifier id;
    
    public BlockDimAttributeImpl(Block block, Identifier id){
        this.block = block;
        this.id = id;
    }
    
    @Override
    public Block getBlock(){
        return block;
    }
    
    @Override
    public Identifier getId(){
        return id;
    }
    
    @Override
    public ItemStack getPreviewStack(){
        return new ItemStack(block);
    }
}
