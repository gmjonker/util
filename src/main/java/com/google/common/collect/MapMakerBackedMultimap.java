// BROKE WHEN UPDATED GUAVA TO 21

//package com.google.common.collect;
//
//import static com.google.common.base.Preconditions.checkNotNull;
//
//import java.util.Collection;
//import java.util.Collections;
//import java.util.Map.Entry;
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentMap;
//
//import com.google.common.base.Function;
//import com.google.common.base.Supplier;
//
///**
// * A concurrent implementation of {@link SetMultimap} backed by a {@link ConcurrentMap}.
// * <p>
// * Iterators are only weakly consistent.
// * <p>
// * Known issues:
// * <ul>
// * <li>There is a leak in that values-collections are not removed on {@link #remove(Object, Object)}; only on
// * {@link #removeAll(Object)}. It is difficult to see how this can be solved without a data race. This will not cause
// * arbitrary objects to be pinned in memory, since the leak only occurs when all of the objects associated with a
// * certain key are removed; the pinned objects will be sets backed by {@link ConcurrentHashMap}s.
// * </ul>
// * <p>
// * Note: this is in a {@code com.google} package only for access to the package-private skeleton implementation
// * {@link AbstractSetMultimap} and friends.
// * 
// * @author Joe Kearney
// */
//public final class MapMakerBackedMultimap<K, V> extends AbstractSetMultimap<K, V> {
//	private static final long serialVersionUID = 2279566459780644963L;
//	
//	/**
//	 * Default initial capacity per key, the number of values expected to be mapped to a single key. Assuming here only
//	 * small depth in the multimap, as a tradeoff against lots of wasted space in the case where the multimap isn't much
//	 * more than just a map.
//	 */
//	public static final int DEFAULT_INITIAL_VALUES_CAPACITY_PER_KEY = 2;
//	/**
//	 * Default initial capacity of top-level key-to-value-collection mappings, taken from {@link MapMaker}.
//	 */
//	public static final int DEFAULT_INITIAL_KEY_CAPACITY = 16;
//
//	/*
//	 * Expected use is to have many accesses into the key store, but then largely single threaded access to the
//	 * resulting values-collection. So it helps to have a high concurrency level on the top-level map, but we don't need
//	 * much thereafter. Given the potential memory leak issues, we're better off minimising the size of the values-
//	 * collections.
//	 */
//	/** default key concurrency level, taken from {@link MapMaker} */
//	public static final int DEFAULT_KEY_CONCURRENCY_LEVEL = 16;
//	/** default value concurrency level, chosen to be small for memory reasons */
//	public static final int DEFAULT_VALUE_CONCURRENCY_LEVEL = 2;
//
//	/** supplier of values-collections, used by {@link #createCollection()} */
//	private final Supplier<Set<V>> supplier;
//
//	/*
//	 * query
//	 */
//	@Override
//	public Set<V> get(final K key) {
//		/*
//		 * Need to implement our own wrapping here (in order to present a view over the possibly empty/missing backing
//		 * set) because the one given through AbstractMultimap#get(K) isn't threadsafe. We can just delegate through to
//		 * the underlying set, that was formed from a CHMap in createCollection(), since even when it does get removed
//		 * through removeAll or clear, it will get recreated on the next lookup.
//		 */
//		return new ForwardingSet<V>() {
//			@Override
//			protected Set<V> delegate() {
//				return MapMakerBackedMultimap.this.get(key);
//			}
//
//		};
//	}
//	@Override
//	public int size() {
//		/*
//		 * The totalSize field in AbstractMultimap is updated everywhere but only ever read through size(). Given that
//		 * the backing CHMap has a size() method only weakly consistent, we must degenerate this operation to scanning
//		 * through all component collections. Thus the totalSize field will be updated a lot but will not be read.
//		 */
//		int count = 0;
//		for (Entry<K, Collection<V>> entry : backingMap().entrySet()) {
//			count += entry.getValue().size();
//		}
//		return count;
//		// equivalent but using more intermediate objects: Iterators.size(this.values().iterator())
//	}
//
//	/*
//	 * modifications
//	 */
//	@Override
//	public boolean put(K key, V value) {
//		return get(key).add(value);
//	}
//	@Override
//	public boolean putAll(K key, Iterable<? extends V> values) {
//		Collection<V> destinationValuesCollection = get(checkNotNull(key));
//		return Iterables.addAll(destinationValuesCollection, values);
//	}
//	@Override
//	public boolean putAll(Multimap<? extends K, ? extends V> multimap) {
//		@SuppressWarnings("unchecked")
//		// all operations we need are covariant
//		Multimap<K, V> castMultimap = (Multimap<K, V>) multimap;
//
//		boolean wasModified = false;
//		for (Entry<K, Collection<V>> entry : castMultimap.asMap().entrySet()) {
//			wasModified |= putAll(entry.getKey(), entry.getValue());
//		}
//		return wasModified;
//	}
//	@Override
//	public Set<V> removeAll(Object key) {
//		Collection<V> removed = backingMap().remove(key);
//
//		if (removed == null) {
//			return ImmutableSet.of();
//		} else {
//			/* clear the values-collection so that live views are updated */
//			ImmutableSet<V> ret = ImmutableSet.copyOf(removed);
//			removed.clear();
//			return ret;
//		}
//	}
//	@Override
//	public boolean remove(Object key, Object value) {
//		ConcurrentMap<K, Collection<V>> backingMap = backingMap();
//		if (backingMap.containsKey(key)) {
//			/*
//			 * We'd like to be able to remove the empty values-collection here in the case that following removal, the
//			 * collection is now empty. This is pretty tricky since we can't lock the segment - removal after checking
//			 * size gives a race condition not easily contained. This will cause lots of waste when we use different
//			 * keys a lot.
//			 * 
//			 * We could perhaps fix this by using weak values and a collection of known non-empty values-collections.
//			 * Sounds messy. Or we could synchronize on the values-collection, at the cost of any concurrency after the
//			 * initial lookup.
//			 */
//			return backingMap.get(key).remove(value);
//		} else {
//			// optimisation: don't need to create the set in order to check containment!
//			return false;
//		}
//	}
//	@Override
//	public Set<V> replaceValues(K key, Iterable<? extends V> values) {
//		Set<V> newSet = createCollection();
//		Iterables.addAll(newSet, values);
//		Collection<V> replaced = backingMap().put(key, newSet);
//
//		if (replaced == null) {
//			return ImmutableSet.of();
//		} else {
//			return ImmutableSet.copyOf(replaced);
//		}
//	}
//
//	/*
//	 * underlying data store
//	 */
//	/**
//	 * Gets the underlying map, cast appropriately to {@link ConcurrentMap}. The map returned is exactly the map passed
//	 * to the {@linkplain #ConcurrentHashMultimap(ConcurrentMap, Supplier) constructor}.
//	 */
//	@Override
//	ConcurrentMap<K, Collection<V>> backingMap() {
//		return (ConcurrentMap<K, Collection<V>>) super.backingMap();
//	}
//	/*
//	 * createCollection() is used in a number of places, for example to create old-values-collections on returning from
//	 * removeAll(K).
//	 * 
//	 * createCollection(K) is only ever used to create collections that actually go in the backing map, and is used in
//	 * a manner that is not threadsafe. We expect it never to be called, since the backing map creates the
//	 * values-collections on demand, so get(key) will never return null.
//	 * 
//	 * Yes, this is dependent on implementation detail in AbstractMultimap.
//	 */
//	@Override
//	Set<V> createCollection() {
//		return supplier.get();
//	}
//	/**
//	 * <strong>Note:</strong> This method is not expected to be called on a {@link MapMakerBackedMultimap}, and throws
//	 * {@link AssertionError} always.
//	 * <p>
//	 * {@inheritDoc}
//	 */
//	@Override
//	Set<V> createCollection(K key) {
//		throw new AssertionError("Collection should never need to be created through createCollection, "
//				+ "this should happen for each key through the backing MapMaker functionality.");
//	}
//	/**
//	 * Supplier of values-collections.
//	 */
//	private static class ValuesCollectionSupplier<V> implements Supplier<Set<V>> {
//		/**
//		 * number of concurrent accesses, configurable as this will make a large difference to the size of the structure
//		 */
//		private final int expectedValuesPerKey;
//		/** initial capacity of the collections for each value */
//		private final int valuesConcurrencyLevel;
//
//		ValuesCollectionSupplier(int expectedValuesPerKey, int valuesConcurrencyLevel) {
//			this.expectedValuesPerKey = expectedValuesPerKey;
//			this.valuesConcurrencyLevel = valuesConcurrencyLevel;
//		}
//		@Override
//		public Set<V> get() {
//			ConcurrentMap<V, Boolean> underlyingMap = new MapMaker().concurrencyLevel(valuesConcurrencyLevel).initialCapacity(
//					expectedValuesPerKey).makeMap();
//			return Collections.newSetFromMap(underlyingMap);
//		}
//	}
//
//	/*
//	 * factory methods
//	 */
//	/**
//	 * Sole constructor.&nbsp;Creates a new {@link SetMultimap} backed by a {@link MapMaker} computing map that
//	 * generates the keysets, which are backed by a {@link ConcurrentMap}, on demand.
//	 * 
//	 * @param <K> type of the keys in the map
//	 * @param <V> type of the values in the map
//	 * @param backingMap concurrent map backing this multimap
//	 * @param supplier creator of values-collections
//	 * @return the new multimap
//	 */
//	private MapMakerBackedMultimap(ConcurrentMap<K, Collection<V>> backingMap, Supplier<Set<V>> supplier) {
//		super(backingMap);
//		this.supplier = supplier;
//	}
//	/**
//	 * Creates a new {@link SetMultimap} backed by a {@link MapMaker} computing map that generates the keysets, which
//	 * are backed by a {@link ConcurrentMap}, on demand.
//	 * 
//	 * @param <K> type of the keys in the map
//	 * @param <V> type of the values in the map
//	 * @return the new multimap
//	 */
//	public static <K, V> MapMakerBackedMultimap<K, V> create() {
//		return create(DEFAULT_INITIAL_KEY_CAPACITY, DEFAULT_INITIAL_VALUES_CAPACITY_PER_KEY,
//				DEFAULT_KEY_CONCURRENCY_LEVEL, DEFAULT_VALUE_CONCURRENCY_LEVEL);
//	}
//	/**
//	 * Base factory method.&nbsp;Creates a new {@link SetMultimap} backed by a {@link MapMaker} computing map that
//	 * generates the keysets, which are backed by a {@link ConcurrentMap}, on demand.
//	 * 
//	 * @param <K> type of the keys in the map
//	 * @param <V> type of the values in the map
//	 * @param expectedKeys initial capacity of mappings from key to collection of values
//	 * @param expectedValuesPerKey initial capacity of the values-collections for each key
//	 * @param keyConcurrencyLevel number of allowed concurrent accesses without blocking to the key store, that is,
//	 *            through {@link #get(Object) get()}
//	 * @param valueConcurrencyLevel number of allowed concurrent accesses without blocking to the values-collections,
//	 *            that is, operations on the collections returned from {@link #get(Object) get()}
//	 * @return the new multimap
//	 */
//	public static <K, V> MapMakerBackedMultimap<K, V> create(int expectedKeys, int expectedValuesPerKey,
//			int keyConcurrencyLevel, int valueConcurrencyLevel) {
//		/*
//		 * Is it worth doing this with a supplier? Has the advantage that it encalsulates the expected size variables.
//		 */
//
//		final ValuesCollectionSupplier<V> supplier = new ValuesCollectionSupplier<V>(expectedValuesPerKey,
//				valueConcurrencyLevel);
//
//		final ConcurrentMap<K, Collection<V>> backingMap = new MapMaker().initialCapacity(expectedKeys).concurrencyLevel(
//				keyConcurrencyLevel).makeComputingMap(new Function<Object, Collection<V>>() {
//			@Override
//			public Collection<V> apply(Object input) {
//				return supplier.get();
//			}
//		});
//
//		return new MapMakerBackedMultimap<K, V>(backingMap, supplier);
//	}
//}
