package net.gudenau.minecraft.dims.block;

import net.gudenau.minecraft.dims.Dims;
import net.gudenau.minecraft.dims.block.entity.PortalReceptacleBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * The block that is responsible for creating (and even destroying) portals from anchor items.
 *
 * The rotation logic in here is weird, but it works.
 *
 * @since 0.0.1
 */
public final class PortalReceptacleBlock extends EntityBlock{
    private static final DirectionProperty FACING = Properties.FACING;
    private static final IntProperty ROTATION = Dims.Blocks.Properties.ROTATION4;
    
    public PortalReceptacleBlock(Settings settings){
        super(settings);
        
        setDefaultState(getStateManager().getDefaultState()
            .with(FACING, Direction.NORTH)
            .with(ROTATION, 0)
        );
    }
    
    /**
     * Gets the state for a portal that has the correct axis for a given receptacle state.
     *
     * @param receptacleState The state of a receptacle
     * @return The state of a portal
     */
    public static BlockState getPortalState(BlockState receptacleState){
        if(!receptacleState.isOf(Dims.Blocks.PORTAL_RECEPTACLE)){
            throw new IllegalArgumentException("receptacleState is not a receptacle");
        }
        
        var dirs = PortalBlock.DIRECTIONS[receptacleState.get(FACING).getAxis().ordinal()];
        
        return Dims.Blocks.PORTAL.getDefaultState()
            .with(Properties.AXIS, dirs[receptacleState.get(ROTATION)].getAxis());
    }
    
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit){
        if(world.isClient()){
            return ActionResult.SUCCESS;
        }
        
        var entity = world.getBlockEntity(pos);
        if(entity instanceof PortalReceptacleBlockEntity){
            return ((PortalReceptacleBlockEntity)entity).onUse(player, hand, player.getStackInHand(hand));
        }
        
        return ActionResult.SUCCESS;
    }
    
    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx){
        var state = getDefaultState();
        
        /*
        Here be funky code
        
        So the placement logic has two jobs. To determine which side is down, and which rotation faces the player. The
        rotation depends on the "down" side of the block.
        
        +--------+
        |\     / |
        |  \ /   |
        |  / \   |
        |/     \ |
        +--------+
        
        The down position is based on a square which each direction being a different placement direction. This square
        is located on the face that the player clicks. After that "down" for the block is determined we iterate over the
        4 possible rotations and figure out the closest to the player's look vector. Cheesy and crappy, but it works
        because there are so few states to check and we don't need to do it often.
         */
        
        var blockPos = ctx.getBlockPos();
        var hitPos = ctx.getHitPos().subtract(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        var hitSide = ctx.getSide();
        // This finds which direction is "down"
        var hitDir = switch(hitSide.getAxis()){
            case X -> {
                var hitX = (float)hitPos.y;
                var hitY = (float)hitPos.z;
                
                if(hitX > hitY){
                    // Right, down
                    if(1 - hitX > hitY){
                        yield Direction.NORTH;
                    }else{
                        yield Direction.UP;
                    }
                }else{
                    // Left, up
                    if(1 - hitX > hitY){
                        yield Direction.DOWN;
                    }else{
                        yield Direction.SOUTH;
                    }
                }
            }
            
            case Y -> {
                var hitX = (float)hitPos.x;
                var hitY = (float)hitPos.z;
    
                if(hitX > hitY){
                    // Right, down
                    if(1 - hitX > hitY){
                        yield Direction.NORTH;
                    }else{
                        yield Direction.EAST;
                    }
                }else{
                    // Left, up
                    if(1 - hitX > hitY){
                        yield Direction.WEST;
                    }else{
                        yield Direction.SOUTH;
                    }
                }
            }
            
            case Z -> {
                var hitX = (float)hitPos.x;
                var hitY = (float)hitPos.y;
    
                if(hitX > hitY){
                    // Right, down
                    if(1 - hitX > hitY){
                        yield Direction.DOWN;
                    }else{
                        yield Direction.EAST;
                    }
                }else{
                    // Left, up
                    if(1 - hitX > hitY){
                        yield Direction.WEST;
                    }else{
                        yield Direction.UP;
                    }
                }
            }
        };
        
        // And this finds the rotation
        var directions = PortalBlock.DIRECTIONS[hitDir.getAxis().ordinal()];
        var lookVector = ctx.getPlayer().getRotationVec(1);
        int bestFit = -1;
        float bestDot = 0;
        for(int i = 0; i < directions.length; i++){
            Direction dir = directions[i];
            var dirVector = dir.getUnitVector();
            float dot = (float)(dirVector.getX() * lookVector.x + dirVector.getY() * lookVector.y + dirVector.getZ() * lookVector.z);
            if(dot < bestDot){
                bestDot = dot;
                bestFit = i;
            }
        }
        
        return state.with(FACING, hitDir).with(ROTATION, bestFit);
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
        builder.add(FACING, ROTATION);
    }
    
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
        return new PortalReceptacleBlockEntity(pos, state);
    }
}
