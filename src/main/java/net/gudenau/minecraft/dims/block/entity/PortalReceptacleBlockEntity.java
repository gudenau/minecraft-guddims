package net.gudenau.minecraft.dims.block.entity;

import java.util.*;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.util.NbtType;
import net.gudenau.minecraft.dims.Dims;
import net.gudenau.minecraft.dims.api.v0.util.DimensionalTeleportTarget;
import net.gudenau.minecraft.dims.block.PortalBlock;
import net.gudenau.minecraft.dims.block.PortalReceptacleBlock;
import net.gudenau.minecraft.dims.item.DimensionAnchorItem;
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
import net.minecraft.util.math.Direction;

public final class PortalReceptacleBlockEntity extends BlockEntity{
    private ItemStack anchor = ItemStack.EMPTY;
    private DimensionalTeleportTarget target;
    
    public PortalReceptacleBlockEntity(BlockPos pos, BlockState state){
        super(Dims.Blocks.Entities.PORTAL_RECEPTACLE, pos, state);
    }
    
    @Override
    public NbtCompound writeNbt(NbtCompound tag){
        tag = super.writeNbt(tag);
        
        tag.putStack("anchor", anchor);
        if(target != null){
            tag.put("target", target.toNbt());
        }
        
        return tag;
    }
    
    @Override
    public void readNbt(NbtCompound tag){
        super.readNbt(tag);
        
        anchor = tag.getStack("anchor");
        if(tag.contains("target", NbtType.COMPOUND)){
            target = DimensionalTeleportTarget.fromNbt(tag.getCompound("target"));
        }else{
            target = null;
        }
    }
    
    public ActionResult onUse(PlayerEntity player, Hand hand, ItemStack stack){
        if(anchor.isEmpty()){
            if(stack.getItem() == Dims.Items.DIMENSION_ANCHOR){
                var target = DimensionAnchorItem.getTarget(stack);
                if(target.isEmpty()){
                    return ActionResult.FAIL;
                }
                this.target = target.get();
                
                anchor = stack.split(1);
                createPortal();
                return ActionResult.SUCCESS;
            }else{
                return ActionResult.FAIL;
            }
        }else{
            if(stack.isEmpty()){
                player.setStackInHand(hand, anchor);
            }else{
                if(!player.getInventory().insertStack(anchor)){
                    var entity = player.dropItem(anchor, true);
                    if(entity != null){
                        world.spawnEntity(entity);
                    }else{
                        return ActionResult.FAIL;
                    }
                }
            }
            anchor = ItemStack.EMPTY;
            deletePortal();
            return ActionResult.SUCCESS;
        }
    }
    
    private void deletePortal(){
        var portalPos = pos.offset(getCachedState().get(Properties.FACING));
        if(world.getBlockState(portalPos).isOf(Dims.Blocks.PORTAL)){
            world.setBlockState(portalPos, Blocks.AIR.getDefaultState());
        }
    }
    
    private void createPortal(){
        var optionalTarget = DimensionAnchorItem.getTarget(anchor);
        if(optionalTarget.isEmpty()){
            return;
        }
        
        Set<BlockPos> checkedPositions = new HashSet<>();
        Set<BlockPos> portalPositions = new HashSet<>();
        List<BlockPos> positionsToCheck = new LinkedList<>();
        positionsToCheck.add(pos.offset(getCachedState().get(Properties.FACING)));
    
        var portalState = PortalReceptacleBlock.getPortalState(getCachedState());
        var directions = PortalBlock.getDirections(portalState);
        
        while(!positionsToCheck.isEmpty()){
            var pos = positionsToCheck.remove(0);
            if(checkedPositions.add(pos)){
                var state = world.getBlockState(pos);
                if(state.isAir()){
                    portalPositions.add(pos);
                    Stream.of(directions)
                        .map(pos::offset)
                        .forEach(positionsToCheck::add);
                }else if(!Dims.Tags.PORTAL_FRAME.contains(state.getBlock())){
                    // Make a fart noise or something
                    return;
                }
            }
        }
        
        var target = optionalTarget.get();
        for(var pos : portalPositions){
            world.setBlockState(pos, portalState, 0);
            var be = world.getBlockEntity(pos);
            ((PortalBlockEntity)be).setTarget(target);
        }
    }
}
