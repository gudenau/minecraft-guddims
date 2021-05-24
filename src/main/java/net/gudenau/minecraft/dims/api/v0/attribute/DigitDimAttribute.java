package net.gudenau.minecraft.dims.api.v0.attribute;

import java.util.List;

public interface DigitDimAttribute extends DimAttribute{
    int getValue();
    
    @Override
    default DimAttributeType getType(){
        return DimAttributeType.DIGIT;
    }
    
    static int getIntValue(List<DigitDimAttribute> attributes){
        int value = 0;
        for(DigitDimAttribute attribute : attributes){
            value *= 10;
            value += attribute.getValue();
        }
        return value;
    }
}
