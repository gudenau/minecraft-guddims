package gudDims.extensions.net.minecraft.text.Text;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import net.minecraft.text.Text;

@Extension
public class TextExtension {
  /**
   * A shortcut to Text.of that uses String.format for the text.
   *
   * @param format The printf format
   * @param args The arguments to process
   * @return The text instance
   */
  @Extension public static Text formated(String format, Object... args) {
    return Text.of(String.format(format, args));
  }
}