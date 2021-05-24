package gudDims.extensions.net.minecraft.nbt.NbtCompound;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

@Extension
public class NbtCompoundExtension {
  public static Identifier getIdentifier(@This NbtCompound thiz, String key) {
    return new Identifier(thiz.getString(key));
  }
  
  public static void putIdentifier(@This NbtCompound thiz, String key, Identifier value){
    thiz.putString(key, value.toString());
  }
  
  public static ItemStack getStack(@This NbtCompound thiz, String key){
    return ItemStack.fromNbt(thiz.getCompound(key));
  }
  
  public static void putStack(@This NbtCompound thiz, String key, ItemStack stack){
    thiz.put(key, stack.writeNbt(new NbtCompound()));
  }
}