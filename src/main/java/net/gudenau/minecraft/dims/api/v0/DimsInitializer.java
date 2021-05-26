package net.gudenau.minecraft.dims.api.v0;

/**
 * Used for mods that want to add specilized attributes.
 *
 * Not required for things that have vanilla registries, like blocks or biomes.
 *
 * @since 0.0.3
 */
public interface DimsInitializer{
    void initDims(DimRegistry registry);
}
