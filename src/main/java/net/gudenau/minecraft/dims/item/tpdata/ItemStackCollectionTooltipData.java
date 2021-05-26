package net.gudenau.minecraft.dims.item.tpdata;

import java.util.Collection;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvironmentInterface;
import net.gudenau.minecraft.dims.api.v0.client.CustomTooltipData;
import net.gudenau.minecraft.dims.item.tpcomponent.ItemStackCollectionTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;
import net.minecraft.item.ItemStack;

/**
 * A generic tooltip component that renders a collection of items on hover.
 *
 * @since 0.0.1
 */
@EnvironmentInterface(value = EnvType.CLIENT, itf = CustomTooltipData.class)
public final class ItemStackCollectionTooltipData implements TooltipData, CustomTooltipData{
    private final Collection<ItemStack> items;
    
    public ItemStackCollectionTooltipData(Collection<ItemStack> items){
        this.items = items;
    }
    
    public ItemStackCollectionTooltipData(Stream<ItemStack> items){
        this.items = items.toList();
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public TooltipComponent getTooltipComponent(){
        return new ItemStackCollectionTooltipComponent(items);
    }
}
