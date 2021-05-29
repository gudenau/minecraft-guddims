package net.gudenau.minecraft.dims.api.v0.attribute;

import net.gudenau.minecraft.dims.api.v0.controller.DimController;

/**
 * A generic controller attribute.
 *
 * For example, the biome controller or weather attributes.
 *
 * @since 0.0.1
 */
public interface ControllerDimAttribute<T extends DimAttribute, C extends DimController<T>> extends DimAttribute{
    /**
     * Checks if an attribute would be a valid property of this controller.
     *
     * Used for configuration of this controller.
     *
     * @param attribute The attribute to check
     * @return True if valid, false if invalid
     */
    @Deprecated(forRemoval = true)
    default boolean isPropertyValid(DimAttribute attribute){
        return getController().isPropertyValid(attribute);
    }
    
    /**
     * Gets the {@link DimController} for this attribute.
     *
     * @return The controller
     */
    C getController();
}
