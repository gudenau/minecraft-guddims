package net.gudenau.minecraft.dims.api.v0.util;

/**
 * A simple int range implementation.
 *
 * @since 0.0.1
 */
public final record IntRange(int lower, int upper){
    /**
     * Creates an IntRage with a single valid value.
     *
     * @param value The valid value
     * @return The new range
     */
    public static IntRange of(int value){
        return new IntRange(value, value);
    }
    
    /**
     * Creates an IntRange with a range of values.
     *
     * @param lower The lower bound
     * @param upper The upper bound
     * @return The new range
     * @throws IllegalArgumentException If the range is invalid
     */
    public static IntRange of(int lower, int upper){
        if(upper < lower){
            throw new IllegalArgumentException("Upper was a smaller value than upper");
        }
        
        return new IntRange(lower, upper);
    }
    
    /**
     * Checks if a value falls within this range.
     *
     * @param value The value to check
     * @return True if lower <= value <= upper
     */
    public boolean isValid(int value){
        return value >= lower && value <= upper;
    }
    
    /**
     * Checks if a value falls under this range.
     *
     * @param value The value to check
     * @return True if value < lower
     */
    public boolean isUnder(int value){
        return value < lower;
    }
    
    /**
     * Checks if a value falls above this range.
     *
     * @param value The value to check
     * @return True if value > upper
     */
    public boolean isOver(int value){
        return value > upper;
    }
}
