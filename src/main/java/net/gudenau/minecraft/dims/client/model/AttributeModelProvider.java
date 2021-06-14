package net.gudenau.minecraft.dims.client.model;

import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import static net.gudenau.minecraft.dims.Dims.MOD_ID;

public final class AttributeModelProvider implements ModelResourceProvider{
    private static final Identifier MODEL_ID = new Identifier(MOD_ID, "item/dimension_attribute");
    
    @Override
    public @Nullable UnbakedModel loadModelResource(Identifier resourceId, ModelProviderContext context){
        return resourceId.equals(MODEL_ID) ? new AttributeItemModel(context) : null;
    }
}
