package net.gudenau.minecraft.dims.api.v0.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.tooltip.TooltipComponent;

@Environment(EnvType.CLIENT)
public interface CustomTooltipData{
    TooltipComponent getTooltipComponent();
}
