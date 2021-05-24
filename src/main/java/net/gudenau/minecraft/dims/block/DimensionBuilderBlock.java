package net.gudenau.minecraft.dims.block;

import net.gudenau.minecraft.dims.Dims;
import net.gudenau.minecraft.dims.block.entity.DimensionBuilderBlockEntity;
import net.gudenau.minecraft.dims.item.DimensionAttributeItem;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class DimensionBuilderBlock extends HorizontalFacingEntityBlock{
    public DimensionBuilderBlock(Settings settings){
        super(settings);
    }
    
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
        return new DimensionBuilderBlockEntity(pos, state);
    }
    
    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : checkType(type, Dims.Blocks.Entities.DIMENSION_BUILDER, DimensionBuilderBlockEntity::tick);
    }
    
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit){
        if(world.isClient()){
            return ActionResult.SUCCESS;
        }
        
        var rawEntity = world.getBlockEntity(pos);
        if(!(rawEntity instanceof DimensionBuilderBlockEntity)){
            return ActionResult.FAIL;
        }
    
        var entity = (DimensionBuilderBlockEntity)rawEntity;
        boolean building = entity.isBuilding();
        
        if(building){
            return ActionResult.FAIL;
        }
        
        if(player.isSneaking()){
            entity.build();
        }else{
            if(entity.getStack(0).isEmpty()){
                var stack = player.getStackInHand(hand);
                if(stack.isEmpty() || !(stack.getItem() instanceof DimensionAttributeItem)){
                    return ActionResult.FAIL;
                }
    
                var copy = stack.copy();
                copy.setCount(1);
                entity.setStack(entity.size(), copy);
                stack.decrement(1);
            }else{
                var result = entity.removeStack(0);
                if(!player.getInventory().insertStack(result)){
                    var droppedItem = player.dropItem(result, true);
                    if(droppedItem != null){
                        world.spawnEntity(droppedItem);
                    }else{
                        entity.setStack(0, result);
                    }
                }
            }
        }
        
        return ActionResult.SUCCESS;
    }
}
