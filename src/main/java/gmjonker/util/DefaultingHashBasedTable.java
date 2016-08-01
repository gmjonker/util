package gmjonker.util;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class DefaultingHashBasedTable<R, C, V> implements Table<R, C, V>
{
    public static <R, C, V> DefaultingHashBasedTable<R, C, V> create(V defaultValue)
    {
        return new DefaultingHashBasedTable<>(HashBasedTable.create(), defaultValue);
    }

    public static <R, C, V> DefaultingHashBasedTable<R, C, V> create(int expectedRows, int expectedCellsPerRow, V defaultValue)
    {
        return new DefaultingHashBasedTable<>(HashBasedTable.create(expectedRows, expectedCellsPerRow), defaultValue);
    }

    public static <R, C, V> DefaultingHashBasedTable<R, C, V> create(Table<? extends R, ? extends C, ? extends V> table, V defaultValue)
    {
        return new DefaultingHashBasedTable<>(HashBasedTable.create(table), defaultValue);
    }





    private HashBasedTable<R, C, V> hashBasedTable;
    private V defaultValue;

    @SuppressWarnings("WeakerAccess")
    public DefaultingHashBasedTable(HashBasedTable<R, C, V> hashBasedTable, V defaultValue)
    {
        this.hashBasedTable = hashBasedTable;
        this.defaultValue = defaultValue;
    }




    public V get(@Nullable Object rowKey, @Nullable Object columnKey)
    {
        return or(hashBasedTable.get(rowKey, columnKey), defaultValue);
    }

    public Set<Table.Cell<R, C, V>> cellSet()
    {
        return hashBasedTable.cellSet();
    }

    public Set<R> rowKeySet()
    {
        return hashBasedTable.rowKeySet();
    }

    public V remove(@Nullable Object rowKey, @Nullable Object columnKey)
    {
        return hashBasedTable.remove(rowKey, columnKey);
    }

    public int size()
    {
        return hashBasedTable.size();
    }

    public void putAll(Table<? extends R, ? extends C, ? extends V> table)
    {
        hashBasedTable.putAll(table);
    }

    public V put(R rowKey, C columnKey, V value)
    {
        return hashBasedTable.put(rowKey, columnKey, value);
    }

    public DefaultingMap<R, V> column(C columnKey)
    {
        return new DefaultingMapWrapper<>(hashBasedTable.column(columnKey), defaultValue);
    }

    public boolean isEmpty()
    {
        return hashBasedTable.isEmpty();
    }

    public boolean contains(@Nullable Object rowKey, @Nullable Object columnKey)
    {
        return hashBasedTable.contains(rowKey, columnKey);
    }

    public void clear()
    {
        hashBasedTable.clear();
    }

    public Set<C> columnKeySet()
    {
        return hashBasedTable.columnKeySet();
    }

    public boolean containsColumn(@Nullable Object columnKey)
    {
        return hashBasedTable.containsColumn(columnKey);
    }

    public boolean containsRow(@Nullable Object rowKey)
    {
        return hashBasedTable.containsRow(rowKey);
    }

    public boolean containsValue(@Nullable Object value)
    {
        return hashBasedTable.containsValue(value);
    }

    public Map<C, Map<R, V>> columnMap()
    {
        throw new RuntimeException("Not implemented");
    }

    public Map<R, Map<C, V>> rowMap()
    {
        throw new RuntimeException("Not implemented");
    }

    public DefaultingMap<C, V> row(R rowKey)
    {
        return new DefaultingMapWrapper<>(hashBasedTable.row(rowKey), defaultValue);
    }

    public Collection<V> values()
    {
        return hashBasedTable.values();
    }

    @Override
    public String toString()
    {
        return "DefaultingHashBasedTable{" +
                "hashBasedTable=" + hashBasedTable +
                ", defaultValue=" + defaultValue +
                '}';
    }

    private V or(V v, V defaultValue)
    {
        if (v == null) {
            return defaultValue;
        } else {
            return v;
        }
    }
}
