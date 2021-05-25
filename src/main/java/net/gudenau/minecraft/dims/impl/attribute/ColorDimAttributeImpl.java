package net.gudenau.minecraft.dims.impl.attribute;

import net.gudenau.minecraft.dims.api.v0.attribute.ColorDimAttribute;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

/**
 * The backing implementation to the color attribute interface.
 *
 * @since 0.0.1
 */
public final class ColorDimAttributeImpl implements ColorDimAttribute{
    private final DyeColor color;
    private final Identifier id;
    private final Item item;
    
    public ColorDimAttributeImpl(DyeColor color, Identifier id){
        this.color = color;
        this.id = id;
        // Bad but only done at init, so not super bad.
        this.item = switch(color){
            case WHITE -> Items.WHITE_DYE;
            case ORANGE -> Items.ORANGE_DYE;
            case MAGENTA -> Items.MAGENTA_DYE;
            case LIGHT_BLUE -> Items.LIGHT_BLUE_DYE;
            case YELLOW -> Items.YELLOW_DYE;
            case LIME -> Items.LIME_DYE;
            case PINK -> Items.PINK_DYE;
            case GRAY -> Items.GRAY_DYE;
            case LIGHT_GRAY -> Items.LIGHT_GRAY_DYE;
            case CYAN -> Items.CYAN_DYE;
            case PURPLE -> Items.PURPLE_DYE;
            case BLUE -> Items.BLUE_DYE;
            case BROWN -> Items.BROWN_DYE;
            case GREEN -> Items.GREEN_DYE;
            case RED -> Items.RED_DYE;
            case BLACK -> Items.BLACK_DYE;
        };
    }
    
    @Override
    public DyeColor getColor(){
        return color;
    }
    
    @Override
    public Identifier getId(){
        return id;
    }
    
    @Override
    public ItemStack getPreviewStack(){
        return new ItemStack(item);
    }
}
