package net.gudenau.minecraft.dims.block.entity;

import java.util.Optional;
import net.fabricmc.fabric.api.util.NbtType;
import net.gudenau.minecraft.dims.Dims;
import net.gudenau.minecraft.dims.api.v0.util.DimensionalTeleportTarget;
import net.gudenau.minecraft.dims.util.MiscStuff;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * The block entity responsible for teleporting entities that touch a portal from this mod.
 *
 * @since 0.0.1
 */
public final class PortalBlockEntity extends BlockEntity{
    /**
     * The target of this portal, can be null.
     */
    @Nullable private DimensionalTeleportTarget target;
    
    public PortalBlockEntity(BlockPos pos, BlockState state){
        super(Dims.Blocks.Entities.PORTAL, pos, state);
    }
    
    @Override
    public void writeNbt(NbtCompound tag){
        super.writeNbt(tag);
        if(target != null){
            tag.put("target", target.toNbt());
        }
    }
    
    @Override
    public void readNbt(NbtCompound tag){
        super.readNbt(tag);
        if(tag.contains("target", NbtType.COMPOUND)){
            target = DimensionalTeleportTarget.fromNbt(tag.getCompound("target"));
        }else{
            target = null;
        }
    }
    
    /**
     * Teleports an entity if this target is not null.
     *
     * @param entity The entity to teleport
     */
    public void teleport(Entity entity){
        if(target != null){
            MiscStuff.teleportEntity(entity, target);
        }
    }
    
    /**
     * Sets the target of this portal to the provided destination.
     *
     * @param target The new target
     */
    public void setTarget(DimensionalTeleportTarget target){
        this.target = target;
        markDirty();
    }
    
    /**
     * Gets the current target, if it exists.
     *
     * @return The target or empty
     */
    public Optional<DimensionalTeleportTarget> getTarget(){
        return Optional.ofNullable(target);
    }
}
