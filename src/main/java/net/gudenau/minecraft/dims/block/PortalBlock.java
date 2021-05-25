package net.gudenau.minecraft.dims.block;

import java.util.Arrays;
import net.gudenau.minecraft.dims.Dims;
import net.gudenau.minecraft.dims.block.entity.PortalBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

/**
 * A custom portal block that allows for intradimensional travel.
 *
 * @since 0.0.1
 */
public final class PortalBlock extends EntityBlock{
    private static final EnumProperty<Direction.Axis> AXIS = Properties.AXIS;
    
    /**
     * We want to make the portal skinny like vanilla ones after all.
     */
    private static final VoxelShape[] SHAPES;
    static{
        SHAPES = new VoxelShape[Direction.Axis.values().length];
        SHAPES[Direction.Axis.X.ordinal()] = Block.createCuboidShape(7, 0, 0, 9, 16, 16);
        SHAPES[Direction.Axis.Y.ordinal()] = Block.createCuboidShape(0, 7, 0, 16, 9, 16);
        SHAPES[Direction.Axis.Z.ordinal()] = Block.createCuboidShape(0, 0, 7, 16, 16, 9);
    }
    
    /**
     * Used in creation and neighbor update logic.
     */
    static final Direction[][] DIRECTIONS;
    static{
        DIRECTIONS = new Direction[3][4];
        Direction.Axis[] values = Direction.Axis.values();
        for(int i = 0; i < values.length; i++){
            Direction.Axis axis = values[i];
            var directions = DIRECTIONS[i];
            int o = 0;
            for(var dir : Direction.values()){
                if(dir.getAxis() != axis){
                    directions[o] = dir;
                    o++;
                }
            }
        }
    }
    
    public PortalBlock(Settings settings){
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(AXIS, Direction.Axis.X));
    }
    
    /**
     * Gets an array of directions that are the four neighboring blocks that "connect" to the provided state.
     *
     * @param portalState The state of the portal
     * @return The direction array
     */
    public static Direction[] getDirections(BlockState portalState){
        if(!portalState.isOf(Dims.Blocks.PORTAL)){
            throw new IllegalArgumentException("portalState is not a portal");
        }
        
        var dirs = DIRECTIONS[portalState.get(AXIS).ordinal()];
        return Arrays.copyOf(dirs, dirs.length);
    }
    
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
        return SHAPES[state.get(AXIS).ordinal()];
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
        builder.add(AXIS);
    }
    
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
        return new PortalBlockEntity(pos, state);
    }
    
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity){
        if(world.isClient()){
            return;
        }
        
        // Teleport the entity!
        var rawEntity = world.getBlockEntity(pos);
        if(rawEntity instanceof PortalBlockEntity){
            ((PortalBlockEntity)rawEntity).teleport(entity);
        }
    }
    
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos){
        // Make sure the portal is still valid
        Direction.Axis dirAxis = direction.getAxis();
        Direction.Axis portalAxis = state.get(AXIS);
        if(dirAxis != portalAxis){
            if(!Dims.Tags.PORTAL_FRAME.contains(neighborState.getBlock())){
                return Blocks.AIR.getDefaultState();
            }
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }
}
