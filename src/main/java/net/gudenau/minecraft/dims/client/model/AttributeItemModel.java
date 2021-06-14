package net.gudenau.minecraft.dims.client.model;

import com.mojang.datafixers.util.Pair;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.gudenau.minecraft.dims.api.v0.attribute.DimAttribute;
import net.gudenau.minecraft.dims.api.v0.attribute.DimAttributeType;
import net.gudenau.minecraft.dims.impl.DimRegistryImpl;
import net.gudenau.minecraft.dims.item.DimensionAttributeItem;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import static net.gudenau.minecraft.dims.Dims.MOD_ID;

public class AttributeItemModel implements UnbakedModel, BakedModel, FabricBakedModel{
    private final Map<DimAttribute, BakedModel> modelMap = new HashMap<>();
    private Map<Identifier, UnbakedModel> unbakedModelMap;
    private Map<Identifier, BakedModel> minecraftModelMap;
    private ModelTransformation transformation;
    
    public AttributeItemModel(ModelProviderContext context){
        try{
            var attributes = DimRegistryImpl.INSTANCE.getAttributes(
                DimAttributeType.COLOR,
                DimAttributeType.BIOME_CONTROLLER,
                DimAttributeType.DIGIT,
                DimAttributeType.BOOLEAN,
                DimAttributeType.WEATHER,
                DimAttributeType.SKYLIGHT,
                DimAttributeType.CELESTIAL,
                DimAttributeType.CELESTIAL_PROPERTY
            );
            unbakedModelMap = attributes.stream()
                .map((attribute)->{
                    var id = attribute.getId();
                    try{
                        return Map.entry(id, context.loadModel(new Identifier(id.getNamespace(), "item/dimension_attribute_" + id.getPath())));
                    }catch(Throwable e){
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(HashMap::new, (map, entry)->map.put(entry.getKey(), entry.getValue()), HashMap::putAll);
            Consumer<Identifier> modelAdder = (id)->
                unbakedModelMap.put(id, context.loadModel(new Identifier(id.getNamespace(), "item/dimension_attribute_" + id.getPath())));
            modelAdder.accept(new Identifier(MOD_ID, "block"));
            modelAdder.accept(new Identifier(MOD_ID, "biome"));
            modelAdder.accept(new Identifier(MOD_ID, "fluid"));
        }catch(Throwable t){
            t.printStackTrace();
            System.exit(1);
        }
    }
    
    // UnbakedModel
    
    @Override
    public Collection<Identifier> getModelDependencies(){
        var set = unbakedModelMap.values().stream()
            .flatMap((model)->model.getModelDependencies().stream())
            .collect(Collectors.toSet());
        unbakedModelMap.keySet().stream()
            .map((id)->new Identifier(id.getNamespace(), "item/dimension_attribute_" + id.getPath()))
            .forEach(set::add);
        return set;
    }
    
    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences){
        var deps = unbakedModelMap.values().stream()
            .flatMap((model)->model.getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences).stream())
            .collect(Collectors.toUnmodifiableSet());
        
        return deps;
    }
    
    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId){
        minecraftModelMap = loader.getBakedModelMap();
    
        Function<Identifier, BakedModel> modelLoader = (id)->
            loader.bake(new Identifier(id.getNamespace(), "item/dimension_attribute_" + id.getPath()), ModelRotation.X0_Y0);
    
        var blockModel = modelLoader.apply(new Identifier(MOD_ID, "block"));
        var fluidModel = modelLoader.apply(new Identifier(MOD_ID, "fluid"));
        var biomeModel = modelLoader.apply(new Identifier(MOD_ID, "biome"));
    
        transformation = blockModel.getTransformation();
        
        for(var attribute : DimRegistryImpl.INSTANCE.getAttributes(DimAttributeType.values())){
            var bakedModel = switch(attribute.getType()){
                case BLOCK -> blockModel;
                case FLUID -> fluidModel;
                case BIOME -> biomeModel;
                default -> {
                    var id = attribute.getId();
                    yield modelLoader.apply(id);
                }
            };
            modelMap.put(attribute, bakedModel);
        }
    
        return this;
    }
    
    // BakedModel
    
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random){
        return List.of();
    }
    
    @Override
    public boolean useAmbientOcclusion(){
        return true;
    }
    
    @Override
    public boolean hasDepth(){
        return true;
    }
    
    @Override
    public boolean isSideLit(){
        return false;
    }
    
    @Override
    public boolean isBuiltin(){
        return false;
    }
    
    @Override
    public Sprite getSprite(){
        return null;
    }
    
    @Override
    public ModelTransformation getTransformation(){
        return transformation;
    }
    
    @Override
    public ModelOverrideList getOverrides(){
        return ModelOverrideList.EMPTY;
    }
    
    // FabricBakedModel
    
    @Override
    public boolean isVanillaAdapter(){
        return false;
    }
    
    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context){}
    
    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context){
        context.fallbackConsumer().accept(DimensionAttributeItem.getAttribute(stack)
            .map(modelMap::get)
            .orElseGet(()->minecraftModelMap.get(ModelLoader.MISSING_ID))
        );
    }
}
