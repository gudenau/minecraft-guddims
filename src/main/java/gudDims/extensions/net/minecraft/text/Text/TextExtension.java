package gudDims.extensions.net.minecraft.text.Text;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import net.minecraft.text.Text;

@Extension
public class TextExtension {
  @Extension public static Text formated(String format, Object... args) {
    return Text.of(String.format(format, args));
  }
}