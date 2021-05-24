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

public final class AnchorMinterBlock extends Block{
    public AnchorMinterBlock(Settings settings){
        super(settings);
    }
    
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit){
        if(world.isClient()){
            return ActionResult.SUCCESS;
        }
        
        var stack = player.getStackInHand(hand);
        if(stack.getItem() != Dims.Items.DIMENSION_TOKEN){
            return ActionResult.SUCCESS;
        }
    
        var worldKey = DimensionTokenItem.getWorld(stack);
        if(worldKey.isEmpty()){
            return ActionResult.SUCCESS;
        }
    
        var inventory = player.getInventory();
        if(!player.isCreative()){
            var remainingEyes = 12;
            for(int i = 0; i < inventory.size() && remainingEyes > 0; i++){
                var current = inventory.getStack(i);
                if(current.getItem() == Items.ENDER_EYE){
                    remainingEyes -= current.getCount();
                }
            }
            if(remainingEyes > 0){
                return ActionResult.SUCCESS;
            }
        }
        
        var destinationWorld = world.getServer().getWorld(worldKey.get());
        if(destinationWorld == null){
            return ActionResult.SUCCESS;
        }
        
        var spawnPos = destinationWorld.getSpawnPos();
    
        var target = new DimensionalTeleportTarget(
            new Vec3d(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5),
            0, 0,
            destinationWorld.getRegistryKey()
        );
        var anchorStack = new ItemStack(Dims.Items.DIMENSION_ANCHOR);
        DimensionAnchorItem.setTarget(anchorStack, target);
    
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
        
        if(!inventory.insertStack(anchorStack)){
            var itemEntity = player.dropItem(anchorStack, true);
            if(itemEntity != null){
                world.spawnEntity(itemEntity);
            }
        }
        
        return ActionResult.SUCCESS;
    }
}
