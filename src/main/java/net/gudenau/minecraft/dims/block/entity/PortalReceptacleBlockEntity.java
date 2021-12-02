package net.gudenau.minecraft.dims.block.entity;

import java.util.*;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.util.NbtType;
import net.gudenau.minecraft.dims.Dims;
import net.gudenau.minecraft.dims.api.v0.util.DimensionalTeleportTarget;
import net.gudenau.minecraft.dims.block.PortalBlock;
import net.gudenau.minecraft.dims.block.PortalReceptacleBlock;
import net.gudenau.minecraft.dims.item.DimensionAnchorItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

/**
 * The entity for the receptacle block. Responsible for creating and destroying the portal blocks.
 *
 * @since 0.0.1
 */
public final class PortalReceptacleBlockEntity extends BlockEntity{
    /**
     * The anchor that holds the current target.
     */
    private ItemStack anchor = ItemStack.EMPTY;
    
    /**
     * The target of the current anchor.
     *
     * Not explicitly needed, but it handy for portal construction.
     */
    private DimensionalTeleportTarget target;
    
    public PortalReceptacleBlockEntity(BlockPos pos, BlockState state){
        super(Dims.Blocks.Entities.PORTAL_RECEPTACLE, pos, state);
    }
    
    @Override
    public void writeNbt(NbtCompound tag){
        super.writeNbt(tag);
        
        tag.put("anchor", anchor.writeNbt(new NbtCompound()));
        if(target != null){
            tag.put("target", target.toNbt());
        }
    }
    
    @Override
    public void readNbt(NbtCompound tag){
        super.readNbt(tag);
        
        anchor = ItemStack.fromNbt(tag.getCompound("anchor"));
        if(tag.contains("target", NbtType.COMPOUND)){
            target = DimensionalTeleportTarget.fromNbt(tag.getCompound("target"));
        }else{
            target = null;
        }
    }
    
    /**
     * Called by the block, it's here so the block isn't constantly calling and accessing things in here. (The isolation
     * is nice in general though)
     *
     * @param player The player that interacted with the block
     * @param hand The hand the player used
     * @param stack The stack in the hand
     * @return The action result of the action
     */
    public ActionResult onUse(PlayerEntity player, Hand hand, ItemStack stack){
        // No current target
        if(anchor.isEmpty()){
            if(stack.getItem() == Dims.Items.DIMENSION_ANCHOR){
                var target = DimensionAnchorItem.getTarget(stack);
                // Always a chance that the anchor is not setup
                if(target.isEmpty()){
                    return ActionResult.FAIL;
                }
                this.target = target.get();
                
                // Take an anchor, even though it's a 1 stack item things happen...
                anchor = stack.split(1);
                createPortal();
                return ActionResult.SUCCESS;
            }else{
                return ActionResult.FAIL;
            }
        }else{
            // Take the anchor out and give it to the player
            if(stack.isEmpty()){
                player.setStackInHand(hand, anchor);
            }else{
                if(!player.getInventory().insertStack(anchor)){
                    var entity = player.dropItem(anchor, true);
                    if(entity != null){
                        world.spawnEntity(entity);
                    }else{
                        // Well somehow we could not give it to the player, just bail so it isn't lost forever
                        return ActionResult.FAIL;
                    }
                }
            }
            anchor = ItemStack.EMPTY;
            deletePortal();
            return ActionResult.SUCCESS;
        }
    }
    
    /**
     * Removes the portal, if it exists.
     */
    private void deletePortal(){
        var portalPos = pos.offset(getCachedState().get(Properties.FACING));
        if(world.getBlockState(portalPos).isOf(Dims.Blocks.PORTAL)){
            world.setBlockState(portalPos, Blocks.AIR.getDefaultState());
        }
    }
    
    /**
     * Creates a new portal.
     *
     * TODO Limit the size of the portal
     */
    private void createPortal(){
        var optionalTarget = DimensionAnchorItem.getTarget(anchor);
        if(optionalTarget.isEmpty()){
            // How did this happen, bail
            return;
        }
        
        // Is there a better way?
        Set<BlockPos> checkedPositions = new HashSet<>();
        Set<BlockPos> portalPositions = new HashSet<>();
        List<BlockPos> positionsToCheck = new LinkedList<>();
        // The initial position to check
        positionsToCheck.add(pos.offset(getCachedState().get(Properties.FACING)));
    
        var portalState = PortalReceptacleBlock.getPortalState(getCachedState());
        var directions = PortalBlock.getDirections(portalState);
        
        while(!positionsToCheck.isEmpty()){
            var pos = positionsToCheck.remove(0);
            // Don't check the same block multiple times
            if(checkedPositions.add(pos)){
                var state = world.getBlockState(pos);
                if(state.isAir()){
                    // Cool this is air, it gets to be a portal block
                    portalPositions.add(pos);
                    // And add all the adjacent blocks to the check list
                    Stream.of(directions)
                        .map(pos::offset)
                        .forEach(positionsToCheck::add);
                }else if(!Dims.Tags.PORTAL_FRAME.contains(state.getBlock())){
                    // TODO Make a fart noise or something
                    return;
                }
            }
        }
        
        // Portal frame is valid, create the portal
        var target = optionalTarget.get();
        for(var pos : portalPositions){
            world.setBlockState(pos, portalState, Block.NOTIFY_LISTENERS);
            var be = world.getBlockEntity(pos);
            ((PortalBlockEntity)be).setTarget(target);
        }
    }
}
