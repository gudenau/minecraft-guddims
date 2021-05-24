package net.gudenau.minecraft.dims.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.gudenau.minecraft.dims.api.v0.client.CustomTooltipData;
import net.minecraft.client.gui.tooltip.BundleTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.BundleTooltipData;
import net.minecraft.client.item.TooltipData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Environment(EnvType.CLIENT)
@Mixin(TooltipComponent.class)
public interface TooltipComponentMixin{
    /**
     * @reason error: Injector in interface is unsupported
     * @author gudenau
     */
    @Overwrite
    static TooltipComponent of(TooltipData data) {
        if(data instanceof BundleTooltipData){
            return new BundleTooltipComponent((BundleTooltipData)data);
        }else if(data instanceof CustomTooltipData){
            return ((CustomTooltipData)data).getTooltipComponent();
        }else{
            throw new IllegalArgumentException("Unknown TooltipComponent");
        }
    }
}
