package net.gudenau.minecraft.dims.block;

import net.gudenau.minecraft.dims.Dims;
import net.gudenau.minecraft.dims.api.v0.util.DimensionalTeleportTarget;
import net.gudenau.minecraft.dims.item.DimensionAnchorItem;
import net.gudenau.minecraft.dims.item.DimensionTokenItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * This block is used to create an anchor from 12 ender eyes and a token (not consumed)
 *
 * @since 0.0.1
 */
public final class AnchorMinterBlock extends Block{
    public AnchorMinterBlock(Settings settings){
        super(settings);
    }
    
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit){
        // Server only, no exceptions
        if(world.isClient()){
            return ActionResult.SUCCESS;
        }
        
        // Check if it's a token being used
        var stack = player.getStackInHand(hand);
        if(stack.getItem() != Dims.Items.DIMENSION_TOKEN){
            return ActionResult.SUCCESS;
        }
    
        // Check if the token has a dimension, it should...
        var worldKey = DimensionTokenItem.getWorld(stack);
        if(worldKey.isEmpty()){
            return ActionResult.SUCCESS;
        }
    
        // Check if the player has 12 (or more) ender eyes or is creative
        var inventory = player.getInventory();
        if(!player.isCreative()){
            var remainingEyes = 12;
            for(int i = 0; i < inventory.size() && remainingEyes > 0; i++){
                var current = inventory.getStack(i);
                if(current.getItem() == Items.ENDER_EYE){
                    // Negative is fine here
                    remainingEyes -= current.getCount();
                }
            }
            if(remainingEyes > 0){
                return ActionResult.SUCCESS;
            }
        }
        
        // Get a handle to the world now that the requirements are satisfied
        var destinationWorld = world.getServer().getWorld(worldKey.get());
        if(destinationWorld == null){
            return ActionResult.SUCCESS;
        }
        
        // Get the spawn position of the token
        var spawnPos = destinationWorld.getSpawnPos();
    
        // and create a target out of it
        var target = new DimensionalTeleportTarget(
            new Vec3d(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5),
            0, 0,
            destinationWorld.getRegistryKey()
        );
        // Create the new stack and set the target
        var anchorStack = new ItemStack(Dims.Items.DIMENSION_ANCHOR);
        DimensionAnchorItem.setTarget(anchorStack, target);
    
        // Remove the ender eyes if the player is not in creative
        if(!player.isCreative()){
            int remainingEyes = 12;
            for(int i = 0; i < inventory.size() && remainingEyes > 0; i++){
                var current = inventory.getStack(i);
                if(current.getItem() == Items.ENDER_EYE){
                    int slice = Math.min(remainingEyes, current.getCount());
                    current.decrement(slice);
                    remainingEyes -= slice;
                }
            }
        }
        
        // And give the anchor to the player
        if(!inventory.insertStack(anchorStack)){
            var itemEntity = player.dropItem(anchorStack, true);
            if(itemEntity != null){
                world.spawnEntity(itemEntity);
            }
        }
        
        return ActionResult.SUCCESS;
    }
}
