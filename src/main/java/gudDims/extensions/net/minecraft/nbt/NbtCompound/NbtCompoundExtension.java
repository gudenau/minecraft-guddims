package gudDims.extensions.net.minecraft.nbt.NbtCompound;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;

@Extension
public class NbtCompoundExtension {
  /**
   * Gets an identifier from this compound tag.
   *
   * This is the same as checking for a tag and creating an identifier from that tag.
   *
   * @param key The name of the tag
   * @return The identifier, if present
   */
  public static Identifier getIdentifier(@This NbtCompound thiz, String key) {
    var entry = thiz.get(key);
    return entry instanceof NbtString ? new Identifier(entry.asString()) : null;
  }
  
  /**
   * Puts an identifier into this compound tag.
   *
   * Works the same as putting the result of value.toString at key.
   *
   * @param key The name of the tag
   * @param value The identifier to write
   */
  public static void putIdentifier(@This NbtCompound thiz, String key, Identifier value){
    thiz.putString(key, value.toString());
  }
  
  /**
   * Gets an item stack from this compound tag.
   *
   * This is the same as checking if it exists, then using ItemStack.fromNbt
   *
   * @param key The name of the tag
   * @return The stack, or empty
   */
  public static ItemStack getStack(@This NbtCompound thiz, String key){
    var tag = thiz.get(key);
    return tag instanceof NbtCompound ? ItemStack.fromNbt((NbtCompound)tag) : ItemStack.EMPTY;
  }
  
  /**
   * Puts an item stack in this compound tag.
   *
   * Works the same as putting the tag from ItemStack.writeNbt.
   *
   * @param key The name of the tag
   * @param stack The stack to put
   */
  public static void putStack(@This NbtCompound thiz, String key, ItemStack stack){
    thiz.put(key, stack.writeNbt(new NbtCompound()));
  }
}