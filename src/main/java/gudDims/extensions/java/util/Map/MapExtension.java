package gudDims.extensions.java.util.Map;

import java.util.Optional;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import java.util.Map;

@Extension
public class MapExtension {
  public static <K, V> Optional<V> getOptional(@This Map<K, V> thiz, K key) {
    return Optional.ofNullable(thiz.get(key));
  }
}