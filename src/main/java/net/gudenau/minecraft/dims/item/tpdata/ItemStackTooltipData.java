package net.gudenau.minecraft.dims.item.tpdata;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.EnvironmentInterface;
import net.gudenau.minecraft.dims.api.v0.client.CustomTooltipData;
import net.gudenau.minecraft.dims.item.tpcomponent.ItemStackTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;
import net.minecraft.item.ItemStack;

@EnvironmentInterface(value = EnvType.CLIENT, itf = CustomTooltipData.class)
public final class ItemStackTooltipData implements TooltipData, CustomTooltipData{
    private final ItemStack stack;
    
    public ItemStackTooltipData(ItemStack stack){
        this.stack = stack;
    }
    
    @Override
    public TooltipComponent getTooltipComponent(){
        return new ItemStackTooltipComponent(stack);
    }
}
