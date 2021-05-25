package net.gudenau.minecraft.dims.api.v0.attribute;

/**
 * A generic controller attribute.
 *
 * For example, the biome controller or weather attributes.
 *
 * @since 0.0.1
 */
public interface ControllerDimAttribute extends DimAttribute{
    /**
     * Checks if an attribute would be a valid property of this controller.
     *
     * Used for configuration of this controller.
     *
     * @param attribute The attribute to check
     * @return True if valid, false if invalid
     */
    boolean isPropertyValid(DimAttribute attribute);
}
