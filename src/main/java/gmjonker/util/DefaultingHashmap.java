package gmjonker.util;

import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * A hashmap that returns a default value for non-existing keys.
 */
public class DefaultingHashmap<K, V> extends HashMap<K, V> implements DefaultingMap<K, V>
{
    // In general, .75 is a good load factor for hashmaps. So we initialize our hashmap with a capacity of 1/.75 times
    // the number of items that we know are going to be in it, for optimal time and space costs.
    private static final double HASHMAP_CAPACITY_FACTOR = 1.0 / .75;

    @Nonnull private final V defaultValue;

    public DefaultingHashmap(@Nonnull V defaultValue)
    {
        super();
        this.defaultValue = defaultValue;
    }

    /**
     * Initialize a column with its expected size. A factor is applied to achieve best performance.
     **/
    public DefaultingHashmap(int expectedSize, @Nonnull V defaultValue)
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

    @Nonnull
    public V getDefaultValue()
    {
        return defaultValue;
    }

    // What should the implementation of values() be when the hashmap is empty? Empty set is maybe most logical,
    // but a singleton set with the default value makes this map more monady.
    @Override
    public Collection<V> values()
    {
        Collection<V> values = super.values();
        if (CollectionUtils.isEmpty(values))
            return Collections.singletonList(defaultValue);
        return values;
    }

    @Override
    public String toString()
    {
        return "DefaultingHashmap{" +
                "defaultValue=" + defaultValue +
                ", values=" + super.toString() +
                '}';
    }
}
