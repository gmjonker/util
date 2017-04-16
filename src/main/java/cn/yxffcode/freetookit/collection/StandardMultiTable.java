package cn.yxffcode.freetookit.collection;

import cn.yxffcode.freetookit.lang.NullSupplier;
import com.google.common.base.Supplier;
import com.google.common.collect.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static gmjonker.util.CollectionsUtil.map;

/**
 * Based on original from gaohang.
 */
public class StandardMultiTable<R, C, V> implements MultiTable<R,C,V>
{

    private final Table<R, C, Collection<V>> backTable;
    private Supplier<Collection<V>> valueCollectionSupplier;

    private int size;

    protected StandardMultiTable(Supplier<Table<R, C, Collection<V>>> tableSupplier,
            Supplier<Collection<V>> valueCollectionSupplier)
    {
        this.backTable = tableSupplier.get();
        this.valueCollectionSupplier = valueCollectionSupplier;
    }

    @Override
    public boolean contains(Object rowKey, Object columnKey)
    {
        return backTable.contains(rowKey, columnKey);
    }

    @Override
    public boolean containsRow(Object rowKey)
    {
        return backTable.containsRow(rowKey);
    }

    @Override
    public boolean containsColumn(Object columnKey)
    {
        return backTable.containsColumn(columnKey);
    }

    @Override
    public boolean containsValue(Object value)
    {
        for (Map.Entry<R, ? extends Multimap<C, V>> re : rowMap().entrySet()) {
            for (Map.Entry<C, Collection<V>> ce : re.getValue().asMap().entrySet()) {
                if (ce.getValue().contains(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Collection<V> get(Object rowKey, Object columnKey)
    {
        return backTable.get(rowKey, columnKey);
    }

    @Override
    public boolean isEmpty()
    {
        return backTable.isEmpty();
    }

    @Override
    public int size()
    {
        return size;
    }

    @Override
    public void clear()
    {
        backTable.clear();
        size = 0;
    }

    @Override
    public void put(R rowKey, C columnKey, V value)
    {
        Collection<V> values = backTable.get(rowKey, columnKey);
        if (values == null) {
            backTable.put(rowKey, columnKey, values = valueCollectionSupplier.get());
        }
        values.add(value);
        ++size;
    }

    @Override
    public void putAll(R rowKey, C columnKey, Iterable<? extends V> values)
    {
        checkNotNull(rowKey);
        checkNotNull(columnKey);
        checkNotNull(values);
        for (V value : values) {
            put(rowKey, columnKey, value);
        }
    }

    @Override
    public void putAll(MultiTable<? extends R, ? extends C, ? extends V> table)
    {
        checkNotNull(table);
        if (table.isEmpty()) {
            return;
        }
        for (Map.Entry<? extends R, ? extends Multimap<? extends C, ? extends V>> re : table.rowMap().entrySet()) {
            for (Map.Entry<? extends C, ? extends Collection<? extends V>> ce : re.getValue().asMap().entrySet()) {
                putAll(re.getKey(), ce.getKey(), ce.getValue());
            }
        }
    }

    @Override
    public void remove(Object rowKey, Object columnKey, Object value)
    {
        Collection<V> values = backTable.get(rowKey, columnKey);
        if (values == null) {
            return;
        }
        if (values.remove(value)) {
            --size;
        }
        if (values.isEmpty()) {
            backTable.remove(rowKey, columnKey);
        }
    }

    @Override
    public Collection<V> remove(Object rowKey, Object columnKey)
    {
        Collection<V> values = backTable.remove(rowKey, columnKey);
        if (values == null) {
            return Collections.emptyList();
        }
        size -= values.size();
        return values;
    }

//    @Override
//    public Multimap<C, V> row(R rowKey)
//    {
//        final Map<C, Collection<V>> row = backTable.row(rowKey);
//        if (row == null) {
//            return ImmutableMultimap.of();
//        }
//        // Bug
//        return Multimaps.unmodifiableMultimap(Multimaps.newMultimap(row, NullSupplier.<Collection<V>>getInstance()));
//    }
//
//    @Override
//    public Multimap<R, V> column(C columnKey)
//    {
//        final Map<R, Collection<V>> column = backTable.column(columnKey);
//        if (column == null) {
//            return ImmutableMultimap.of();
//        }
//        // Bug
//        return Multimaps.unmodifiableMultimap(Multimaps.newMultimap(column, NullSupplier.<Collection<V>>getInstance()));
//    }
//
    @Override
    public Map<C, Collection<V>> row(R rowKey)
    {
        final Map<C, Collection<V>> row = backTable.row(rowKey);
        if (row == null) {
            return ImmutableMap.of();
        }
        return row;
    }

    @Override
    public Map<R, Collection<V>> column(C columnKey)
    {
        final Map<R, Collection<V>> column = backTable.column(columnKey);
        if (column == null) {
            return ImmutableMap.of();
        }
        return column;
    }

    @Override
    public Set<R> rowKeySet()
    {
        return backTable.rowKeySet();
    }

    @Override
    public Set<C> columnKeySet()
    {
        return backTable.columnKeySet();
    }

    @Override
    public Collection<V> values()
    {
        Collection<Collection<V>> values = backTable.values();
        return GroupCollection.create(values);
    }

    @Override
    public Map<R, Multimap<C, V>> rowMap()
    {
        final Map<R, Map<C, Collection<V>>> rowMap = backTable.rowMap();
        if (rowMap.isEmpty()) {
            return Collections.emptyMap();
        }
        return Maps.transformValues(
                rowMap, 
                input -> Multimaps.unmodifiableMultimap(Multimaps.newMultimap(input, NullSupplier.getInstance()))
        );
    }

    @Override
    public Map<C, Multimap<R, V>> columnMap()
    {
        final Map<C, Map<R, Collection<V>>> colMap = backTable.columnMap();
        if (colMap.isEmpty()) {
            return Collections.emptyMap();
        }
        return Maps.transformValues(
                colMap, 
                input -> Multimaps.unmodifiableMultimap(Multimaps.newMultimap(input, NullSupplier.getInstance()))
        );
    }

    @Override
    public String toString()
    {
        return "StandardMultiTable{" +
                "backTable=" + backTable +
                '}';
    }

    @Override
    public <V2> String toString(Function<V, V2> valueMapper)
    {
        return "StandardMultiTable{" +
                "backTable=" + map(backTable, collection -> map(collection, valueMapper)) +
                '}';
    }
}
