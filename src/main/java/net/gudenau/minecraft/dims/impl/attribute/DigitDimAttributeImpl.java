package net.gudenau.minecraft.dims.impl.attribute;

import java.util.Locale;
import net.gudenau.minecraft.dims.api.v0.attribute.DigitDimAttribute;
import net.minecraft.util.Identifier;

import static net.gudenau.minecraft.dims.Dims.MOD_ID;

/**
 * The backing implementation to the digit attribute interface.
 *
 * @since 0.0.1
 */
public final class DigitDimAttributeImpl implements DigitDimAttribute{
    private final int value;
    private final DigitType type;
    private final Identifier id;
    
    public DigitDimAttributeImpl(int value){
        this.value = value;
        this.type = DigitType.NUMERIC;
        id = new Identifier(MOD_ID, "digit" + value);
    }
    
    public DigitDimAttributeImpl(DigitType type){
        this.value = 0;
        this.type = type;
        id = new Identifier(MOD_ID, "digit" + type.name().toLowerCase(Locale.ROOT));
    }
    
    @Override
    public int getValue(){
        return value;
    }
    
    @Override
    public DigitType getDigitType(){
        return type;
    }
    
    @Override
    public Identifier getId(){
        return id;
    }
}
