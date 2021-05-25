package gudDims.extensions.java.util.Map;

import java.util.Optional;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import java.util.Map;

@Extension
public class MapExtension {
  /**
   * No idea why this doesn't actually exist in the STD lib, kinda obvious!
   *
   * Gets a value that is associated with the key, or empty if it does not exist.
   *
   * @param key The key who's associated value is to be returned
   * @return The value wrapped in an Optional, or empty
   */
  public static <K, V> Optional<V> getOptional(@This Map<K, V> thiz, K key) {
    return Optional.ofNullable(thiz.get(key));
  }
}