package net.gudenau.minecraft.dims.api.v0.util;

public final record IntRange(int lower, int upper){
    public static IntRange of(int value){
        return new IntRange(value, value);
    }
    
    public static IntRange of(int lower, int upper){
        if(upper < lower){
            throw new IllegalArgumentException("Upper was a smaller value than upper");
        }
        
        return new IntRange(lower, upper);
    }
    
    public boolean isValid(int value){
        return value >= lower && value <= upper;
    }
    
    public boolean isUnder(int value){
        return value < lower;
    }
    
    public boolean isOver(int value){
        return value > upper;
    }
}
