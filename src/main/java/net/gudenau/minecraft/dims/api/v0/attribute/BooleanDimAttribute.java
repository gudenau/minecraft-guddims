package net.gudenau.minecraft.dims.api.v0.attribute;

public interface BooleanDimAttribute extends DimAttribute{
    boolean getBoolean();
    
    @Override
    default DimAttributeType getType(){
        return DimAttributeType.BOOLEAN;
    }
}
