package net.gudenau.minecraft.dims.client.renderer.blockentity;

import net.gudenau.minecraft.dims.block.entity.PortalBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Matrix4f;

public final class PortalBlockEntityRenderer implements BlockEntityRenderer<PortalBlockEntity>{
    @Override
    public void render(PortalBlockEntity endPortalBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j){
        Matrix4f matrix4f = matrixStack.peek().getModel();
        render(endPortalBlockEntity, matrix4f, vertexConsumerProvider.getBuffer(RenderLayer.getEndPortal()));
    }
    
    private void render(PortalBlockEntity entity, Matrix4f matrix4f, VertexConsumer vertices){
        float firstValue = 0.4375F;
        float secondValue = 0.5625F;
        
        switch(entity.getCachedState().get(Properties.AXIS)){
            case X -> {
                vertices.vertex(matrix4f, firstValue, 0, 0).next();
                vertices.vertex(matrix4f, firstValue, 0, 1).next();
                vertices.vertex(matrix4f, firstValue, 1, 1).next();
                vertices.vertex(matrix4f, firstValue, 1, 0).next();
    
                vertices.vertex(matrix4f, secondValue, 1, 0).next();
                vertices.vertex(matrix4f, secondValue, 1, 1).next();
                vertices.vertex(matrix4f, secondValue, 0, 1).next();
                vertices.vertex(matrix4f, secondValue, 0, 0).next();
            }
            
            case Y -> {
                vertices.vertex(matrix4f, 0, firstValue, 0).next();
                vertices.vertex(matrix4f, 1, firstValue, 0).next();
                vertices.vertex(matrix4f, 1, firstValue, 1).next();
                vertices.vertex(matrix4f, 0, firstValue, 1).next();
    
                vertices.vertex(matrix4f, 0, secondValue, 1).next();
                vertices.vertex(matrix4f, 1, secondValue, 1).next();
                vertices.vertex(matrix4f, 1, secondValue, 0).next();
                vertices.vertex(matrix4f, 0, secondValue, 0).next();
            }
            
            case Z -> {
                vertices.vertex(matrix4f, 0, 1, firstValue).next();
                vertices.vertex(matrix4f, 1, 1, firstValue).next();
                vertices.vertex(matrix4f, 1, 0, firstValue).next();
                vertices.vertex(matrix4f, 0, 0, firstValue).next();
    
                vertices.vertex(matrix4f, 0, 0, secondValue).next();
                vertices.vertex(matrix4f, 1, 0, secondValue).next();
                vertices.vertex(matrix4f, 1, 1, secondValue).next();
                vertices.vertex(matrix4f, 0, 1, secondValue).next();
            }
        }
    }
}
