package net.gudenau.minecraft.dims.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * A block with rotation about the vertical axis.
 *
 * Better than the MC one because it doesn't assume you added the state stuff yourself, otherwise identical.
 *
 * @since 0.0.1
 */
public abstract class HorizontalFacingBlock extends Block{
    protected static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    
    protected HorizontalFacingBlock(Settings settings){
        super(settings);
        setDefaultState(stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }
    
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }
    
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
        super.appendProperties(builder);
        builder.add(FACING);
    }
    
    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx){
        return getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
    }
}
