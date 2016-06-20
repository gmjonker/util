package gmjonker.util;

import javax.annotation.Nonnull;
import java.util.HashMap;

/**
 * A hashmap that returns a default value for non-existing keys.
 */
public class DefaultHashmap<K, V> extends HashMap<K, V>
{
    // In general, .75 is a good load factor for hashmaps. So we initialize our hashmap with a capacity of 1/.75 times
    // the number of items that we know are going to be in it, for optimal time and space costs.
    private static final double HASHMAP_CAPACITY_FACTOR = 1.0 / .75;

    @Nonnull private final V defaultValue;

    public DefaultHashmap(@Nonnull V defaultValue)
    {
        super();
        this.defaultValue = defaultValue;
    }

    /**
     * Initialize a column with its expected size. A factor is applied to achieve best performance.
     **/
    public DefaultHashmap(int expectedSize, @Nonnull V defaultValue)
    {
        super((int) (expectedSize * HASHMAP_CAPACITY_FACTOR));
        this.defaultValue = defaultValue;
    }

    @Override
    @Nonnull
    public V get(Object key)
    {
        return containsKey(key) ? super.get(key) : defaultValue;
    }
}
