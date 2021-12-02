package net.gudenau.minecraft.dims.item;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.gudenau.minecraft.dims.Dims;
import net.gudenau.minecraft.dims.api.v0.*;
import net.gudenau.minecraft.dims.api.v0.attribute.*;
import net.gudenau.minecraft.dims.item.tpdata.ItemStackTooltipData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * An item that represents a single dimension attribute at a time.
 *
 * @since 0.0.1
 */
public final class DimensionAttributeItem extends Item{
    
    public DimensionAttributeItem(Settings settings){
        super(settings);
    }
    
    @Override
    public String getTranslationKey(){
        return "item.gud_dims.dimension_attribute";
    }
    
    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks){
        // Since it is once instance of an item for a type, we need custom rules for the tabs.
        if(group == Dims.Items.BIOME_GROUP){
            DimRegistry.getInstance().getAttributes(DimAttributeType.BIOME).stream()
                .map(DimensionAttributeItem::getStack)
                .forEach(stacks::add);
        }else if(group == Dims.Items.CONTROLLER_GROUP){
            DimRegistry.getInstance().getAttributes(
                DimAttributeType.BIOME_CONTROLLER,
                DimAttributeType.SKYLIGHT,
                DimAttributeType.WEATHER,
                DimAttributeType.FEATURE_CONTROLLER
            ).stream()
                .map(DimensionAttributeItem::getStack)
                .forEach(stacks::add);
        }else if(group == Dims.Items.MISC_GROUP){
            DimRegistry.getInstance().getAttributes(
                DimAttributeType.BOOLEAN,
                DimAttributeType.COLOR,
                DimAttributeType.DIGIT
            ).stream()
                .map(DimensionAttributeItem::getStack)
                .forEach(stacks::add);
        }else if(group == Dims.Items.BLOCK_GROUP){
            DimRegistry.getInstance().getAttributes(DimAttributeType.BLOCK).stream()
                .map(DimensionAttributeItem::getStack)
                .forEach(stacks::add);
        }else if(group == Dims.Items.CELESTIAL_GROUP){
            DimRegistry.getInstance().getAttributes(
                DimAttributeType.CELESTIAL,
                DimAttributeType.CELESTIAL_PROPERTY
            ).stream()
                .map(DimensionAttributeItem::getStack)
                .forEach(stacks::add);
        }else if(group == Dims.Items.FLUID_GROUP){
            DimRegistry.getInstance().getAttributes(DimAttributeType.FLUID).stream()
                .map(DimensionAttributeItem::getStack)
                .forEach(stacks::add);
        }else if(group == Dims.Items.FEATURE_GROUP){
            DimRegistry.getInstance().getAttributes(DimAttributeType.FEATURE).stream()
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
        
        var tag = stack.getNbt();
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
        var stack = new ItemStack(Dims.Items.DIMENSION_ATTRIBUTE);
        var tag = stack.getOrCreateNbt();
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
                case FEATURE -> {
                    var feature = ((FeatureDimAttribute)attribute).getFeature().get();
                    var featureRegistry = MinecraftClient.getInstance().world.getRegistryManager().getMutable(Registry.CONFIGURED_FEATURE_KEY);
                    var featureId = featureRegistry.getId(feature);
                    var featurePath = featureId.getPath();
                    if(featurePath.contains("/")){
                        featurePath = featurePath.substring(featurePath.lastIndexOf('/') + 1);
                    }
                    var featureName = Stream.of(featurePath.split("_"))
                        .map((segment)->{
                            if(segment.length() > 1){
                                return segment.substring(0, 1).toUpperCase(Locale.ROOT) + segment.substring(1);
                            }else{
                                return segment.toUpperCase(Locale.ROOT);
                            }
                        })
                        .collect(Collectors.joining(" "));
                    
                    tooltip.add(
                        new TranslatableText("tooltip.gud_dims.type.feature")
                            .append(Text.of(": "))
                            .append(Text.of(featureName))
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
