package gmjonker.util;

import javax.annotation.Nonnull;
import java.util.Map;

public interface DefaultingMap<K, V> extends Map<K, V>
{
    @Nonnull V getDefaultValue();
}
