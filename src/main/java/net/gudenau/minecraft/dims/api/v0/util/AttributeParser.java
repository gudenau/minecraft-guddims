package net.gudenau.minecraft.dims.api.v0.util;

import java.util.List;
import net.gudenau.minecraft.dims.api.v0.attribute.DimAttribute;
import net.gudenau.minecraft.dims.impl.util.AttributeParserImpl;

/**
 * A helper for parsing tokens. Mostly useful for handling numbers.
 *
 * @since 0.0.4
 */
public interface AttributeParser{
    /**
     * Creates a new parser from a list of attributes.
     *
     * @param attributes The attributes to parse
     * @return The parser
     */
    static AttributeParser of(List<DimAttribute> attributes){
        return new AttributeParserImpl(attributes);
    }
    
    /**
     * Reads the next token if needed and returns the type of the token.
     *
     * @return The token type
     */
    TokenType next();
    
    /**
     * Gets an integer from the list of attributes.
     *
     * @return The parsed integer
     */
    int getInt();
    
    /**
     * Gets an float from the list of attributes.
     *
     * @return The parsed float
     */
    float getFloat();
    
    /**
     * Gets a boolean from the list of attributes.
     *
     * @return The parsed boolean
     */
    boolean getBoolean();
    
    /**
     * Gets an attribute from the list of attributes, if it does not belong to a different method.
     *
     * @return The parsed attribute
     */
    DimAttribute getAttribute();
    
    /**
     * Gets the amount of remaining attributes to parse.
     *
     * @return The count of remaining attributes
     */
    int getRemainingAttributeCount();
    
    /**
     * The possible token types to parse.
     *
     * @since 0.0.4
     */
    enum TokenType{
        /**
         * The "end of stream" token, signals that there are no more tokens to parse.
         */
        END,
        /**
         * Used internally, means that the last token was consumed or never read.
         */
        EMPTY,
        /**
         * The next parsed attribute was not of another category.
         */
        ATTRIBUTE,
        /**
         * The next attribute is a number of some sort.
         */
        NUMBER,
        /**
         * The next attribute is a boolean value.
         */
        BOOLEAN,
    }
}
