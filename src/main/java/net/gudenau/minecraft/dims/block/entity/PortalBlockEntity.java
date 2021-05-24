package net.gudenau.minecraft.dims.block.entity;

import java.util.Optional;
import net.fabricmc.fabric.api.util.NbtType;
import net.gudenau.minecraft.dims.Dims;
import net.gudenau.minecraft.dims.api.v0.util.DimensionalTeleportTarget;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public final class PortalBlockEntity extends BlockEntity{
    @Nullable private DimensionalTeleportTarget target;
    
    public PortalBlockEntity(BlockPos pos, BlockState state){
        super(Dims.Blocks.Entities.PORTAL, pos, state);
    }
    
    @Override
    public NbtCompound writeNbt(NbtCompound tag){
        tag = super.writeNbt(tag);
        if(target != null){
            tag.put("target", target.toNbt());
        }
        return tag;
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
    
    public void teleport(Entity entity){
        if(target != null){
            entity.teleportToTarget(target);
        }
    }
    
    public void setTarget(DimensionalTeleportTarget target){
        this.target = target;
        markDirty();
    }
    
    public Optional<DimensionalTeleportTarget> getTarget(){
        return Optional.ofNullable(target);
    }
}
