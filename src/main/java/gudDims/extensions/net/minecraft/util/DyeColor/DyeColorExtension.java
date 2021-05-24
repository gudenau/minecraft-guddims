package gudDims.extensions.net.minecraft.util.DyeColor;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import net.minecraft.util.DyeColor;

@Extension
public class DyeColorExtension {
  private static final Object2IntMap<DyeColor> COLORS;
  static{
    var colors = new Object2IntOpenHashMap<DyeColor>(DyeColor.values().length);
    for(var value : DyeColor.values()){
      var components = value.getColorComponents();
      colors.put(value, ((int)(components[0] * 255) << 16) | ((int)(components[1] * 255) << 8) | (int)(components[2] * 255));
    }
    COLORS = Object2IntMaps.unmodifiable(colors);
  }
  
  public static int getColor(@This DyeColor thiz) {
    return COLORS.getInt(thiz);
  }
}