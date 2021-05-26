package net.gudenau.minecraft.dims.client;

import java.util.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.gudenau.minecraft.dims.accessor.client.SpriteAccessor;
import net.gudenau.minecraft.dims.api.v0.DimRegistry;
import net.gudenau.minecraft.dims.api.v0.attribute.BlockDimAttribute;
import net.gudenau.minecraft.dims.api.v0.attribute.DimAttributeType;
import net.gudenau.minecraft.dims.util.MiscStuff;
import net.gudenau.minecraft.dims.util.TripleInt;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;

@Environment(EnvType.CLIENT)
public final class BlockColorCache{
    private static final TripleInt DEFAULT_COLOR = new TripleInt(0xFF00FF, 0, 0);
    
    private BlockColorCache(){}
    
    private static final DimRegistry DIM_REGISTRY = DimRegistry.getInstance();
    
    private static final Map<Block, TripleInt> COLORS = new HashMap<>();
    
    public static void reload(){
        COLORS.clear();
    
        var instance = MinecraftClient.getInstance();
        var blockModels = instance.getBakedModelManager().getBlockModels();
        var random = new Random();
        
        DIM_REGISTRY.<BlockDimAttribute>getAttributes(DimAttributeType.BLOCK).stream()
            .map(BlockDimAttribute::getBlock)
            .forEach((block)->{
                var state = block.getDefaultState();
                var model = blockModels.getModel(state);
                
                int upColor = getColor(model.getQuads(state, Direction.UP, random));
                int rightColor = getColor(model.getQuads(state, Direction.NORTH, random));
                int leftColor = getColor(model.getQuads(state, Direction.EAST, random));
                
                int defaultColor = block.getDefaultMapColor().color;
                
                if(upColor == -1){
                    upColor = getColor(model.getQuads(state, Direction.DOWN, random));
                    if(upColor == -1){
                        upColor = defaultColor;
                    }
                }
                if(rightColor == -1){
                    rightColor = getColor(model.getQuads(state, Direction.SOUTH, random));
                    if(rightColor == -1){
                        rightColor = defaultColor;
                    }
                }
                if(leftColor == -1){
                    leftColor = getColor(model.getQuads(state, Direction.WEST, random));
                    if(leftColor == -1){
                        leftColor = defaultColor;
                    }
                }
                
                COLORS.put(block, new TripleInt(upColor, rightColor, leftColor));
            });
    }
    
    public static TripleInt getColors(Block block){
        return COLORS.getOrDefault(block, DEFAULT_COLOR);
    }
    
    private static int getColor(List<BakedQuad> quads){
        if(quads.isEmpty()){
            return -1;
        }
        
        long red = 0;
        long green = 0;
        long blue = 0;
        long total = 0;
        
        for(var quad : quads){
            var sprite = quad.getSprite();
            var image = ((SpriteAccessor)sprite).getImages()[0];
            var pixels = MiscStuff.getImagePixels(image);
            while(pixels.hasRemaining()){
                var pixel = pixels.get();
                var alpha = (pixel >>> 24);
                if(alpha == 0xFF){
                    blue += pixel & 0xFF;
                    green += (pixel >> 8) & 0xFF;
                    red += (pixel >> 16) & 0xFF;
                    total++;
                }
            }
        }
        
        if(total == 0){
            return -1;
        }
        
        red /= total;
        green /= total;
        blue /= total;
        
        return (int)((red & 0xFF) | ((green << 8) & 0xFF00) | ((blue << 16) & 0xFF0000));
    }
}
