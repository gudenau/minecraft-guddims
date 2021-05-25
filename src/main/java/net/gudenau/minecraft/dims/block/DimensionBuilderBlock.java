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

/**
 * The block that is responsible for creating dimensions for players.
 *
 * @since 0.0.1
 */
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
        // Server side only
        if(world.isClient()){
            return ActionResult.SUCCESS;
        }
        
        // Make sure the block has the entity we need
        var rawEntity = world.getBlockEntity(pos);
        if(!(rawEntity instanceof DimensionBuilderBlockEntity)){
            return ActionResult.FAIL;
        }
    
        // And make sure it is not building a dimension already
        var entity = (DimensionBuilderBlockEntity)rawEntity;
        boolean building = entity.isBuilding();
        
        if(building){
            return ActionResult.FAIL;
        }
        
        // Request a new dimension
        if(player.isSneaking()){
            entity.build();
        }else{
            // Inserting attribute items
            if(entity.getStack(0).isEmpty()){
                // Check if it's actually a dim attribute
                var stack = player.getStackInHand(hand);
                if(stack.isEmpty() || !(stack.getItem() instanceof DimensionAttributeItem)){
                    return ActionResult.FAIL;
                }
    
                entity.setStack(entity.size(), stack.split(1));
            }else{
                // Extract the result
                var result = entity.removeStack(0);
                if(!player.getInventory().insertStack(result)){
                    var droppedItem = player.dropItem(result, true);
                    if(droppedItem != null){
                        world.spawnEntity(droppedItem);
                    }else{
                        // Just in case we can't extract it, put it back
                        entity.setStack(0, result);
                    }
                }
            }
        }
        
        return ActionResult.SUCCESS;
    }
}
