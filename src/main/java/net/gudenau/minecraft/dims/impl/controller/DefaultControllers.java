package net.gudenau.minecraft.dims.impl.controller;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.gudenau.minecraft.dims.api.v0.attribute.BiomeDimAttribute;
import net.gudenau.minecraft.dims.api.v0.controller.DimController;
import net.gudenau.minecraft.dims.impl.controller.biome.CheckerboardBiomeDimControllerImpl;
import net.gudenau.minecraft.dims.impl.controller.biome.SingleBiomeDimControllerImpl;

public final class DefaultControllers{
    private DefaultControllers(){}
    
    public static Set<DimController<?>> createControllers(){
        return Stream.of(
            createBiomeControllers()
        ).flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet());
    }
    
    private static Set<DimController<BiomeDimAttribute>> createBiomeControllers(){
        return Set.of(
            new SingleBiomeDimControllerImpl(),
            new CheckerboardBiomeDimControllerImpl()
        );
    }
}
