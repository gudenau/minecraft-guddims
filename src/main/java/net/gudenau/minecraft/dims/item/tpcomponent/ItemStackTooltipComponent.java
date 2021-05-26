package net.gudenau.minecraft.dims.item.tpcomponent;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.tooltip.BundleTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

/**
 * A generic tooltip component that renders a single items on hover.
 *
 * @since 0.0.1
 */
@Environment(EnvType.CLIENT)
public final class ItemStackTooltipComponent implements TooltipComponent{
    private final ItemStack stack;
    
    public ItemStackTooltipComponent(ItemStack stack){
        this.stack = stack;
    }
    
    public int getHeight() {
        return 20;
    }
    
    public int getWidth(TextRenderer textRenderer) {
        return 20;
    }
    
    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z, TextureManager textureManager){
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BundleTooltipComponent.TEXTURE);
        DrawableHelper.drawTexture(matrices, x, y, z, 0, 0, 18, 18, 128, 128);
        
        itemRenderer.renderInGuiWithOverrides(stack, x + 1, y + 1, 0);
        itemRenderer.renderGuiItemOverlay(textRenderer, stack, x + 1, y + 1);
    }
}
