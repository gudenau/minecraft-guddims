package net.gudenau.minecraft.dims.duck;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.gen.feature.ConfiguredFeature;

public interface BiomeDuck{
    void gud_dims$setFeaturesOverride(List<List<Supplier<ConfiguredFeature<?, ?>>>> features);
}
