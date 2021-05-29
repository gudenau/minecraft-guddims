package net.gudenau.minecraft.dims.api.v0.controller;

import net.gudenau.minecraft.dims.api.v0.attribute.DimAttribute;
import net.minecraft.util.Identifier;

/**
 * An interface for all dimension controllers.
 *
 * @param <T> The type of attribute that corresponds with this controller
 *
 * @since 0.0.3
 */
public interface DimController<T extends DimAttribute>{
    /**
     * Gets the id of this controller.
     *
     * This needs to be static because it is used in serialization.
     *
     * @return The id of this controller.
     */
    Identifier getId();
    
    /**
     * Gets the type of this dimension controller.
     *
     * @return The type of controller
     */
    ControllerType getType();
    
    /**
     * Check if an attribute is valid for a this controller.
     *
     * Used in case you want to pass arguments to the controller.
     *
     * @param attribute The attribute to check
     * @return True if valid, false if not
     */
    boolean isPropertyValid(DimAttribute attribute);
}
