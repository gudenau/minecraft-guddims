package net.gudenau.minecraft.dims.block.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.fabricmc.fabric.api.util.NbtType;
import net.gudenau.minecraft.dims.Dims;
import net.gudenau.minecraft.dims.api.v0.DimRegistry;
import net.gudenau.minecraft.dims.item.DimensionAttributeItem;
import net.gudenau.minecraft.dims.item.DimensionTokenItem;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

public final class DimensionBuilderBlockEntity extends BlockEntity implements SidedInventory{
    private static final int[] SLOTS_EMPTY = new int[0];
    private static final int[] SLOTS_EXTRACT = new int[]{
        0
    };
    
    private final List<ItemStack> attributeItems = new ArrayList<>();
    private ItemStack output = ItemStack.EMPTY;
    private long buildingTime = 0;
    
    public DimensionBuilderBlockEntity(BlockPos pos, BlockState state){
        super(Dims.Blocks.Entities.DIMENSION_BUILDER, pos, state);
    }
    
    @Override
    public NbtCompound writeNbt(NbtCompound tag){
        tag = super.writeNbt(tag);
        
        trimStacks();
        
        tag.put("output", output.writeNbt(new NbtCompound()));
        
        var items = new NbtList();
        attributeItems.stream().sequential()
            .forEach((stack)->items.add(stack.writeNbt(new NbtCompound())));
        tag.put("input", items);
        
        tag.putLong("time", buildingTime);
        
        return tag;
    }
    
    @Override
    public void readNbt(NbtCompound tag){
        super.readNbt(tag);
        
        output = ItemStack.fromNbt(tag.getCompound("output"));
        
        attributeItems.clear();
        tag.getList("input", NbtType.COMPOUND).stream()
            .map((element)->(NbtCompound)element)
            .map(ItemStack::fromNbt)
            .forEach(attributeItems::add);
        
        buildingTime = tag.getLong("time");
    }
    
    public static void tick(World world, BlockPos pos, BlockState state, DimensionBuilderBlockEntity blockEntity){
        if(blockEntity.buildingTime > 0){
            world.forEachPlayer((player)->{
                if(blockEntity.buildingTime != 1){
                    player.sendMessage(Text.formated("Building time has %d ticks left", blockEntity.buildingTime), true);
                }else{
                    player.sendMessage(Text.of("Building time has 1 tick left"), true);
                }
            });
            if(--blockEntity.buildingTime == 0){
                blockEntity.craft();
            }
            blockEntity.markDirty();
        }
    }
    
    private void craft(){
        var attributes = attributeItems.stream()
            .filter((stack)->!stack.isEmpty())
            .map(DimensionAttributeItem::getAttribute)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toUnmodifiableList());
        
        var registry = DimRegistry.getInstance();
        var dimension = registry.createDimension(world.getServer(), attributes);
        if(dimension.isEmpty()){
            return;
        }
        
        world.createExplosion(null, pos.getX(), pos.getY(), pos.getZ(), 5, false, Explosion.DestructionType.NONE);
        //noinspection OptionalGetWithoutIsPresent
        output = DimensionTokenItem.createToken(registry.getDimensionKey(dimension.get()).get(), attributes);
        attributeItems.clear();
    }
    
    @Override
    public int size(){
        return attributeItems.size() + 1;
    }
    
    @Override
    public boolean isEmpty(){
        if(!output.isEmpty()){
            return false;
        }
        return attributeItems.stream().anyMatch((stack)->!stack.isEmpty());
    }
    
    @Override
    public ItemStack getStack(int slot){
        if(slot == 0){
            return output;
        }
        slot--;
        if(slot >= attributeItems.size()){
            return ItemStack.EMPTY;
        }else{
            return attributeItems.get(slot);
        }
    }
    
    @Override
    public ItemStack removeStack(int slot, int amount){
        ItemStack stack;
        if(slot == 0){
            stack = output;
        }else{
            slot--;
            stack = slot >= attributeItems.size() ? ItemStack.EMPTY : attributeItems.get(slot);
        }
        if(stack.isEmpty() && amount > 0){
            var result = stack.split(amount);
            trimStacks();
            markDirty();
            return result;
        }else{
            return ItemStack.EMPTY;
        }
    }
    
    @Override
    public ItemStack removeStack(int slot){
        if(slot == 0){
            var stack = output;
            output = ItemStack.EMPTY;
            markDirty();
            return stack;
        }
        slot--;
        if(slot >= attributeItems.size()){
            return ItemStack.EMPTY;
        }
        var stack = attributeItems.get(slot);
        attributeItems.set(slot, ItemStack.EMPTY);
        trimStacks();
        markDirty();
        return stack;
    }
    
    @Override
    public void setStack(int slot, ItemStack stack){
        if(slot == 0){
            output = stack;
            markDirty();
            return;
        }
        slot--;
        if(slot >= attributeItems.size()){
            while(slot > attributeItems.size()){
                attributeItems.add(ItemStack.EMPTY);
            }
            attributeItems.add(stack);
        }else{
            attributeItems.set(slot, stack);
            if(stack.isEmpty()){
                trimStacks();
            }
        }
        markDirty();
    }
    
    // Never call markDirty in this method
    private void trimStacks(){
        var index = attributeItems.size() - 1;
        while(!attributeItems.isEmpty()){
            var stack = attributeItems.get(index);
            if(stack.isEmpty()){
                attributeItems.remove(index--);
            }else{
                break;
            }
        }
    }
    
    @Override
    public boolean canPlayerUse(PlayerEntity player){
        if(world.getBlockEntity(pos) != this){
            return false;
        }else{
            return player.squaredDistanceTo(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
        }
    }
    
    @Override
    public void clear(){
        attributeItems.clear();
        output = ItemStack.EMPTY;
        markDirty();
    }
    
    @Override
    public int[] getAvailableSlots(Direction side){
        return switch(side){
            case UP -> IntStream.range(1, attributeItems.size() + 2).toArray();
            case DOWN -> SLOTS_EXTRACT;
            default -> SLOTS_EMPTY;
        };
    }
    
    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir){
        return !isBuilding() && output.isEmpty() && slot > 0 && dir == Direction.UP && stack.getItem() instanceof DimensionAttributeItem;
    }
    
    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir){
        return dir == Direction.DOWN && slot == 0;
    }
    
    public boolean isBuilding(){
        return buildingTime > 0;
    }
    
    public void build(){
        buildingTime = attributeItems.size() * 20 + 20;
        markDirty();
    }
}
