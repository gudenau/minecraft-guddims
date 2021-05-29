package net.gudenau.minecraft.dims.item;

import java.util.List;
import java.util.Optional;
import net.gudenau.minecraft.dims.Dims;
import net.gudenau.minecraft.dims.api.v0.*;
import net.gudenau.minecraft.dims.api.v0.attribute.*;
import net.gudenau.minecraft.dims.item.tpdata.ItemStackTooltipData;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * An item that represents a single dimension attribute at a time.
 *
 * @since 0.0.1
 */
public final class DimensionAttributeItem extends Item{
    private final DimAttributeType type;
    
    public DimensionAttributeItem(DimAttributeType type, Settings settings){
        super(settings);
        this.type = type;
    }
    
    @Override
    public String getTranslationKey(){
        return "item.gud_dims.dimension_attribute";
    }
    
    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks){
        // Since it is once instance of an item for a type, we need custom rules for the tabs.
        if(getGroup() == group){
            DimRegistry.getInstance().getAttributes(type).stream()
                .map(DimensionAttributeItem::getStack)
                .forEach(stacks::add);
        }
    }
    
    /**
     * Gets an attribute from a stack, if it is present.
     *
     * @param stack The stack with the attribute
     * @return The attribute or empty
     */
    public static Optional<DimAttribute> getAttribute(ItemStack stack){
        if(!(stack.getItem() instanceof DimensionAttributeItem)){
            return Optional.empty();
        }
        
        var tag = stack.getTag();
        if(tag == null){
            return Optional.empty();
        }
        
        var registry = DimRegistry.getInstance();
        return registry.getAttributeType(new Identifier(tag.getString("AttributeKind")))
            .flatMap(type->registry.getAttribute(type, new Identifier(tag.getString("Attribute"))));
    }
    
    /**
     * Gets a stack that corresponds to an attribute.
     *
     * @param attribute The attribute
     * @return The created stack
     */
    public static ItemStack getStack(DimAttribute attribute){
        var stack = new ItemStack(switch(attribute.getType()){
            case BLOCK -> Dims.Items.DIMENSION_ATTRIBUTE_BLOCK;
            case FLUID -> Dims.Items.DIMENSION_ATTRIBUTE_FLUID;
            case COLOR -> Dims.Items.DIMENSION_ATTRIBUTE_COLOR;
            case BIOME -> Dims.Items.DIMENSION_ATTRIBUTE_BIOME;
            case BIOME_CONTROLLER -> Dims.Items.DIMENSION_ATTRIBUTE_BIOME_CONTROLLER;
            case DIGIT -> Dims.Items.DIMENSION_ATTRIBUTE_DIGIT;
            case BOOLEAN -> Dims.Items.DIMENSION_ATTRIBUTE_BOOLEAN;
            case WEATHER -> Dims.Items.DIMENSION_ATTRIBUTE_WEATHER;
            case SKYLIGHT -> Dims.Items.DIMENSION_ATTRIBUTE_SKYLIGHT;
        });
        var tag = stack.getOrCreateTag();
        tag.putString("AttributeKind", attribute.getType().getId().toString());
        tag.putString("Attribute", attribute.getId().toString());
        return stack;
    }
    
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context){
        var attributeOptional = getAttribute(stack);
        if(attributeOptional.isPresent()){
            var attribute = attributeOptional.get();
            var type = attribute.getType();
            var typeId = type.getId();
            tooltip.add(
                new TranslatableText("tooltip.gud_dims.type")
                    .append(new TranslatableText("tooltip." + typeId.getNamespace() + ".type." + typeId.getPath()))
            );
            
            var attributeId = attribute.getId();
            switch(type){
                case FLUID -> {
                    var fluid = ((FluidDimAttribute)attribute).getFluid();
                    var blockId = Registry.BLOCK.getId(fluid.getDefaultState().getBlockState().getBlock());
                    tooltip.add(
                        new TranslatableText("tooltip.gud_dims.type.fluid")
                            .append(Text.of(": "))
                            .append(new TranslatableText("block." + blockId.getNamespace() + "." + blockId.getPath()))
                    );
                }
                default ->
                    tooltip.add(
                        new TranslatableText("tooltip.gud_dims.type." + typeId.getPath())
                            .append(Text.of(": "))
                            .append(new TranslatableText(typeId.getPath() + "." + attributeId.getNamespace() + "." + attributeId.getPath()))
                    );
            }
        }
    }
    
    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack){
        return getAttribute(stack).map((attribute)->{
            var previewStack = attribute.getPreviewStack();
            if(!previewStack.isEmpty() && !(previewStack.getItem() instanceof DimensionAttributeItem)){
                return new ItemStackTooltipData(previewStack);
            }else{
                return null;
            }
        });
    }
}
