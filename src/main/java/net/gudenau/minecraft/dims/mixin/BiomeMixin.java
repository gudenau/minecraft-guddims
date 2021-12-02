package net.gudenau.minecraft.dims.mixin;

import java.util.*;
import java.util.function.Supplier;
import net.gudenau.minecraft.dims.duck.BiomeDuck;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.gen.random.ChunkRandom;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Biome.class)
public abstract class BiomeMixin implements BiomeDuck{
//    @Shadow @Final private GenerationSettings generationSettings;
//    @Shadow @Final private Map<Integer, List<StructureFeature<?>>> structures;
    @Unique
    private List<List<Supplier<ConfiguredFeature<?, ?>>>> gud_dims$featuresOverride;
    
    @Unique
    @Override
    public void gud_dims$setFeaturesOverride(List<List<Supplier<ConfiguredFeature<?, ?>>>> featuresOverride){
        gud_dims$featuresOverride = featuresOverride;
    }
    
    /*
    @Redirect(
        method = "generateFeatureStep",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/biome/GenerationSettings;getFeatures()Ljava/util/List;"
        )
    )
    private List<?> generateFeatureStep(GenerationSettings generationSettings){
        return gud_dims$featuresOverride == null ? generationSettings.getFeatures() : gud_dims$featuresOverride;
    }
     */
    
    /* *
     * @author FIXME Remove this
     */
    /*
    @Overwrite
    public void generateFeatureStep(StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, ChunkRegion region, long populationSeed, ChunkRandom random, BlockPos origin) {
        var features = generationSettings.getFeatures();
        Registry<ConfiguredFeature<?, ?>> featureRegistry = region.getRegistryManager().get(Registry.CONFIGURED_FEATURE_KEY);
        Registry<StructureFeature<?>> structureRegistry = region.getRegistryManager().get(Registry.STRUCTURE_FEATURE_KEY);
        int featuresCount = GenerationStep.Feature.values().length;
        
        for(int featureStep = 0; featureStep < featuresCount; featureStep++){
            int k = 0;
            if(structureAccessor.shouldGenerateStructures()){
                for(var structureFeature : structures.getOrDefault(featureStep, Collections.emptyList())){
                    random.setDecoratorSeed(populationSeed, k, featureStep);
                    int sectionX = ChunkSectionPos.getSectionCoord(origin.getX());
                    int sectionZ = ChunkSectionPos.getSectionCoord(origin.getZ());
                    int blockX = ChunkSectionPos.getBlockCoord(sectionX);
                    int blockZ = ChunkSectionPos.getBlockCoord(sectionZ);
                    Supplier<String> supplier = ()->{
                        Optional<String> featureKey = structureRegistry.getKey(structureFeature).map(Object::toString);
                        Objects.requireNonNull(structureFeature);
                        return featureKey.orElseGet(structureFeature::toString);
                    };
                    
                    try{
                        int bottomY = region.getBottomY() + 1;
                        int topY = region.getTopY() - 1;
                        region.setCurrentlyGeneratingStructureName(supplier);
                        structureAccessor.getStructuresWithChildren(ChunkSectionPos.from(origin), structureFeature).forEach((structureStart)->{
                            structureStart.generateStructure(region, structureAccessor, chunkGenerator, random, new BlockBox(blockX, bottomY, blockZ, blockX + 15, topY, blockZ + 15), new ChunkPos(sectionX, sectionZ));
                        });
                    }catch(Exception e){
                        var crashReport = CrashReport.create(e, "Feature placement");
                        var section = crashReport.addElement("Feature");
                        Objects.requireNonNull(supplier);
                        section.add("Description", supplier::get);
                        throw new CrashException(crashReport);
                    }
                }
            }
            
            if(features.size() > featureStep){
                for(var featureSupplier : features.get(featureStep)){
                    ConfiguredFeature<?, ?> feature = featureSupplier.get();
                    Supplier<String> keySupplier = ()->{
                        Optional<String> featureKey = featureRegistry.getKey(feature).map(Object::toString);
                        Objects.requireNonNull(feature);
                        return featureKey.orElseGet(feature::toString);
                    };
                    random.setDecoratorSeed(populationSeed, k, featureStep);
                    try{
                        region.setCurrentlyGeneratingStructureName(keySupplier);
                        feature.generate(region, chunkGenerator, random, origin);
                    }catch(Exception e){
                        var crashReport = CrashReport.create(e, "Feature placement");
                        var section = crashReport.addElement("Feature");
                        Objects.requireNonNull(keySupplier);
                        section.add("Description", keySupplier::get);
                        throw new CrashException(crashReport);
                    }
                }
            }
        }
        
        region.setCurrentlyGeneratingStructureName(null);
    }
     */
}
