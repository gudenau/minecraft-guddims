package net.gudenau.minecraft.dims.item;

import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.util.NbtType;
import net.gudenau.minecraft.dims.Dims;
import net.gudenau.minecraft.dims.api.v0.util.DimensionalTeleportTarget;
import net.gudenau.minecraft.dims.block.entity.PortalBlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class DimensionAnchorItem extends Item{
    public DimensionAnchorItem(Settings settings){
        super(settings);
    }
    
    @Override
    public ActionResult useOnBlock(ItemUsageContext context){
        var world = context.getWorld();
        var pos = context.getBlockPos();
        var state = world.getBlockState(pos);
        if(!state.isOf(Dims.Blocks.PORTAL)){
            return super.useOnBlock(context);
        }
        
        var stack = context.getStack();
        var target = getTarget(stack);
        if(target.isPresent()){
            return super.useOnBlock(context);
        }
        
        var entity = world.getBlockEntity(pos);
        if(!(entity instanceof PortalBlockEntity)){
            return super.useOnBlock(context);
        }
        
        ((PortalBlockEntity)entity).getTarget().ifPresent((portalTarget)->setTarget(stack, portalTarget));
        return ActionResult.SUCCESS;
    }
    
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
        var stack = user.getStackInHand(hand);
        
        if(world.isClient){
            return TypedActionResult.success(stack);
        }
        
        if(user.isSneaking()){
            var tag = stack.getTag();
            if(tag != null){
                if(tag.getLong("cooldown") > world.getTime()){
                    stack.setTag(null);
                }else{
                    tag.putLong("cooldown", world.getTime() + 20);
                }
            }
            return TypedActionResult.success(stack);
        }
        
        var targetOptional = getTarget(stack);
        if(targetOptional.isPresent()){
            if(user.isCreative()){
                user.teleportToTarget(targetOptional.get());
            }else{
                var inventory = user.getInventory();
                var slot = inventory.getSlotWithStack(new ItemStack(Items.ENDER_PEARL));
                if(slot != -1){
                    inventory.getStack(slot).decrement(1);
                    user.damage(DamageSource.FALL, 5.0F);
                    user.teleportToTarget(targetOptional.get());
                }
            }
        }else{
            var target = new DimensionalTeleportTarget(
                user.getPos(),
                user.getYaw(),
                user.getPitch(),
                user.world.getRegistryKey()
            );
            setTarget(stack, target);
        }
        
        return TypedActionResult.success(stack);
    }
    
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context){
        if(world == null){
            return;
        }

        getTarget(stack).ifPresent((target)->{
            var pos = target.position();
            tooltip.add(new TranslatableText("tooltip.gud_dims.target.pos", pos.x, pos.y, pos.z));
            var dimension = target.world().getValue();
            tooltip.add(new TranslatableText("tooltip.gud_dims.target.dim", dimension.toString()));
        });
    }
    
    public static Optional<DimensionalTeleportTarget> getTarget(ItemStack stack){
        var tag = stack.getTag();
        if(tag == null){
            return Optional.empty();
        }
    
        if(!tag.contains("target", NbtType.COMPOUND)){
            return Optional.empty();
        }
        
        return Optional.of(DimensionalTeleportTarget.fromNbt(tag.getCompound("target")));
    }
    
    public static void setTarget(ItemStack stack, DimensionalTeleportTarget target){
        var tag = stack.getOrCreateTag();
        tag.put("target", target.toNbt());
    }
}
