package net.gudenau.minecraft.dims.item;

import java.util.*;
import net.fabricmc.fabric.api.util.NbtType;
import net.gudenau.minecraft.dims.Dims;
import net.gudenau.minecraft.dims.api.v0.DimRegistry;
import net.gudenau.minecraft.dims.api.v0.attribute.DimAttribute;
import net.gudenau.minecraft.dims.item.tpdata.ItemStackCollectionTooltipData;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * A token represents a dimension, it can not be used for teleportation directly.
 *
 * @since 0.0.1
 */
public final class DimensionTokenItem extends Item{
    public DimensionTokenItem(Settings settings){
        super(settings);
    }
    
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context){
        getWorld(stack).ifPresent((key)->{
            DimRegistry.getInstance().getDimensionInfo(key).ifPresent((info)->{
                if(info.hasCustomName()){
                    tooltip.add(
                        new TranslatableText("tooltip.gud_dims.world_name")
                            .append(Text.of(": "))
                            .append(Text.of(info.getName()))
                    );
                }
            });
            tooltip.add(new TranslatableText("tooltip.gud_dims.world")
                .append(Text.of(": "))
                .append(Text.of(key.getValue().toString()))
            );
        });
    }
    
    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack){
        return getAttributes(stack).map((attributes)->new ItemStackCollectionTooltipData(attributes.stream().map(DimAttribute::getPreviewStack)));
    }
    
    /**
     * Gets the world registry key that corresponds to the provided token.
     *
     * @param stack The stack to query
     * @return The registry key or empty
     */
    public static Optional<RegistryKey<World>> getWorld(ItemStack stack){
        var tag = stack.getNbt();
        if(tag == null){
            return Optional.empty();
        }
        
        return Optional.of(RegistryKey.of(Registry.WORLD_KEY, new Identifier(tag.getString("world"))));
    }
    
    /**
     * Gets the attributes that where used to create this token.
     *
     * This should be done though the registry.
     *
     * @param stack The stack to query
     * @return The attributes or empty
     */
    @Deprecated(forRemoval = true)
    public static Optional<List<DimAttribute>> getAttributes(ItemStack stack){
        var tag = stack.getNbt();
        if(tag == null){
            return Optional.empty();
        }
        
        var registry = DimRegistry.getInstance();
        var list = new ArrayList<DimAttribute>();
        for(NbtElement element : tag.getList("attributes", NbtType.COMPOUND)){
            var compound = (NbtCompound)element;
            var type = registry.getAttributeType(new Identifier(tag.getString("type")));
            var attribute = type.flatMap((value)->
                registry.getAttribute(value, new Identifier(compound.getString("attribute")))
            );
            if(attribute.isEmpty()){
                return Optional.empty();
            }
            list.add((DimAttribute)attribute.get());
        }
        
        return Optional.ofNullable(list.isEmpty() ? null : Collections.unmodifiableList(list));
    }
    
    /**
     * Creates a token from a registry key and a set of attributes.
     *
     * @param world The registry key
     * @param attributes The attributes
     * @return The new token stack
     */
    @Deprecated(forRemoval = true)
    public static ItemStack createToken(RegistryKey<World> world, List<DimAttribute> attributes){
        ItemStack stack = new ItemStack(Dims.Items.DIMENSION_TOKEN);
        var tag = stack.getOrCreateNbt();
        tag.putString("world", world.getValue().toString());
        var attributeList = new NbtList();
        for(var attribute : attributes){
            var attributeTag = new NbtCompound();
            attributeTag.putString("type", attribute.getType().getId().toString());
            attributeTag.putString("attribute", attribute.getId().toString());
            attributeList.add(attributeTag);
        }
        tag.put("attributes", attributeList);
        return stack;
    }
}
