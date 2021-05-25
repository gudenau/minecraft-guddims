package net.gudenau.minecraft.dims.api.v0.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.tooltip.TooltipComponent;

/**
 * An implementation of a custom tooltip component.
 *
 * This is used to create the bundle-like tooltips used for attributes and tokens.
 */
@Environment(EnvType.CLIENT)
public interface CustomTooltipData{
    TooltipComponent getTooltipComponent();
}
