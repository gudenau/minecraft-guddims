package net.gudenau.minecraft.dims.api.v0.util.collection;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * A simple pair of an object and an integer.
 *
 * Saves a boxing/unboxing step.
 *
 * @param <T> The type of the object
 *
 * @since 0.0.3
 */
public final record ObjectIntPair<T>(T object, int integer){
    /**
     * Creates a new pair from the provided arguments.
     *
     * @param object The object of the pair
     * @param value The int of the pair
     * @param <T> The type of the object
     * @return The created pair
     * @throws NullPointerException If the object was null
     */
    public static <T> ObjectIntPair<T> of(@NotNull T object, int value){
        return new ObjectIntPair<>(object, value);
    }
    
    public ObjectIntPair(T object, int integer){
        this.object = Objects.requireNonNull(object, "Object was null");
        this.integer = integer;
    }
    
    @Override
    public boolean equals(Object obj){
        if(obj == this)
            return true;
        if(obj == null || obj.getClass() != this.getClass())
            return false;
        var that = (ObjectIntPair<?>)obj;
        return Objects.equals(this.object, that.object) &&
               this.integer == that.integer;
    }
    
    @Override
    public int hashCode(){
        return Objects.hash(object, integer);
    }
    
    @Override
    public String toString(){
        return "ObjectIntPair[" +
               "object=" + object + ", " +
               "integer=" + integer + ']';
    }
}
