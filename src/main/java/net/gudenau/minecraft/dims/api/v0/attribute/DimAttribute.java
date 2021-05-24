package net.gudenau.minecraft.dims.api.v0.attribute;

import net.gudenau.minecraft.dims.item.DimensionAttributeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public interface DimAttribute{
    Identifier getId();
    DimAttributeType getType();
    
    default ItemStack getStack(){
        return DimensionAttributeItem.getStack(this);
    }
    
    default ItemStack getPreviewStack(){
        return getStack();
    }
}
