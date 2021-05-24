package net.gudenau.minecraft.dims.item.tpcomponent;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

import static net.minecraft.client.gui.tooltip.BundleTooltipComponent.TEXTURE;

public final class ItemStackCollectionTooltipComponent implements TooltipComponent{
    private final List<ItemStack> stacks;
    
    public ItemStackCollectionTooltipComponent(Collection<ItemStack> stacks){
        this.stacks = stacks.stream()
            .filter((stack)->!stack.isEmpty())
            .collect(Collectors.toUnmodifiableList());
    }
    
    @Override
    public int getHeight(){
        return getRows() * 20 + 6;
    }
    
    @Override
    public int getWidth(TextRenderer textRenderer){
        return getColumns() * 18 + 2;
    }
    
    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z, TextureManager textureManager){
        int columns = getColumns();
        int rows = getRows();
        int k = 0;
    
        for(int row = 0; row < rows; ++row) {
            for(int column = 0; column < columns; ++column) {
                int slotX = x + column * 18 + 1;
                int slotY = y + row * 20 + 1;
                drawSlot(slotX, slotY, k++, textRenderer, matrices, itemRenderer, z);
            }
        }
    
        drawOutline(x, y, columns, rows, matrices, z);
    }
    
    private void drawSlot(int x, int y, int index, TextRenderer textRenderer, MatrixStack matrices, ItemRenderer itemRenderer, int z){
        if(index >= stacks.size()){
            draw(matrices, x, y, z, Sprite.SLOT);
        }else{
            ItemStack itemStack = stacks.get(index);
            draw(matrices, x, y, z, Sprite.SLOT);
            itemRenderer.renderInGuiWithOverrides(itemStack, x + 1, y + 1, index);
            itemRenderer.renderGuiItemOverlay(textRenderer, itemStack, x + 1, y + 1);
        }
    }
    
    private void drawOutline(int x, int y, int columns, int rows, MatrixStack matrices, int z){
        draw(matrices, x, y, z, Sprite.BORDER_CORNER_TOP);
        draw(matrices, x + columns * 18 + 1, y, z, Sprite.BORDER_CORNER_TOP);
        
        for(int j = 0; j < columns; j++){
            draw(matrices, x + 1 + j * 18, y, z, Sprite.BORDER_HORIZONTAL_TOP);
            draw(matrices, x + 1 + j * 18, y + rows * 20, z, Sprite.BORDER_HORIZONTAL_BOTTOM);
        }
        
        for(int j = 0; j < rows; j++){
            draw(matrices, x, y + j * 20 + 1, z, Sprite.BORDER_VERTICAL);
            draw(matrices, x + columns * 18 + 1, y + j * 20 + 1, z, Sprite.BORDER_VERTICAL);
        }
        
        draw(matrices, x, y + rows * 20, z, Sprite.BORDER_CORNER_BOTTOM);
        draw(matrices, x + columns * 18 + 1, y + rows * 20, z, Sprite.BORDER_CORNER_BOTTOM);
    }
    
    private void draw(MatrixStack matrices, int x, int y, int z, Sprite sprite){
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        DrawableHelper.drawTexture(matrices, x, y, z, sprite.u, sprite.v, sprite.width, sprite.height, 128, 128);
    }
    
    private int getColumns(){
        //return Math.max(2, (int)Math.ceil(Math.sqrt(stacks.size() + 1.0D)));
        return Math.min(5, stacks.size());
    }
    
    private int getRows(){
        return (int)Math.ceil((float)Math.min(stacks.size(), 15) / getColumns());
    }
    
    private enum Sprite {
        SLOT(0, 0, 18, 20),
        BORDER_VERTICAL(0, 18, 1, 20),
        BORDER_HORIZONTAL_TOP(0, 20, 18, 1),
        BORDER_HORIZONTAL_BOTTOM(0, 60, 18, 1),
        BORDER_CORNER_TOP(0, 20, 1, 1),
        BORDER_CORNER_BOTTOM(0, 60, 1, 1);
        
        public final int u;
        public final int v;
        public final int width;
        public final int height;
        
        Sprite(int u, int v, int width, int height) {
            this.u = u;
            this.v = v;
            this.width = width;
            this.height = height;
        }
    }
}
