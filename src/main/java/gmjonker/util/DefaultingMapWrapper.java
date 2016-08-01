package gmjonker.util;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

// What is the status of this versus DefaultingHashMap?
@SuppressWarnings("WeakerAccess")
public class DefaultingMapWrapper<K,V> implements DefaultingMap<K,V>
{
    private Map<K,V> map;
    private V defaultValue;

    public DefaultingMapWrapper(Map<K, V> map, V defaultValue)
    {
        this.map = map;
        this.defaultValue = defaultValue;
    }

    @Nonnull
    @Override
    public V getDefaultValue()
    {
        return defaultValue;
    }

    //
    // Delegating methods
    //

    public int size()
    {
        return map.size();
    }

    public boolean containsValue(Object value)
    {
        return map.containsValue(value);
    }

    public Set<K> keySet()
    {
        return map.keySet();
    }

    public void putAll(Map<? extends K, ? extends V> m)
    {
        map.putAll(m);
    }

    public V get(Object key)
    {
        return map.getOrDefault(key, defaultValue);
    }

    public Set<Map.Entry<K, V>> entrySet()
    {
        return map.entrySet();
    }

    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction)
    {
        return map.compute(key, remappingFunction);
    }

    public void forEach(BiConsumer<? super K, ? super V> action)
    {
        map.forEach(action);
    }

    public boolean remove(Object key, Object value)
    {
        return map.remove(key, value);
    }

    public V replace(K key, V value)
    {
        return map.replace(key, value);
    }

    public V put(K key, V value)
    {
        return map.put(key, value);
    }

    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function)
    {
        map.replaceAll(function);
    }

    public boolean replace(K key, V oldValue, V newValue)
    {
        return map.replace(key, oldValue, newValue);
    }

    public boolean isEmpty()
    {
        return map.isEmpty();
    }

    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction)
    {
        return map.merge(key, value, remappingFunction);
    }

    public V putIfAbsent(K key, V value)
    {
        return map.putIfAbsent(key, value);
    }

    public boolean containsKey(Object key)
    {
        return map.containsKey(key);
    }

    public V remove(Object key)
    {
        return map.remove(key);
    }

    // public V getOrDefault(Object key, V defaultValue)
    // {
    //     return map.getOrDefault(key, defaultValue);
    // }

    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction)
    {
        return map.computeIfAbsent(key, mappingFunction);
    }

    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction)
    {
        return map.computeIfPresent(key, remappingFunction);
    }

    public void clear()
    {
        map.clear();
    }

    public Collection<V> values()
    {
        return map.values();
    }

    @Override
    public String toString()
    {
        return "DefaultingMapWrapper{" +
                "defaultValue=" + defaultValue +
                ", map=" + map +
                '}';
    }

    public String toString(Function<K,?> keyMapper)
    {
        return "DefaultingMapWrapper{" +
                "defaultValue=" + defaultValue +
                ", map=" + CollectionsUtil.map(this.map, keyMapper, v -> v)  +
                '}';
    }
}
