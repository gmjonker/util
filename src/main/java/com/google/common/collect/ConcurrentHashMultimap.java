package com.google.common.collect;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.base.Function;

/**
 * Concurrent implementation of {@link SetMultimap} based on the {@link ConcurrentHashMap} implementation from JDK
 * 1.6.0_21.
 * @author Joe Kearney
 * @see ConcurrentHashMap
 * 
 * GJ: Removed accompanying test classes.
 */
public final class ConcurrentHashMultimap<K, V> implements ConcurrentSetMultimap<K, V>, Serializable {
	/*
	 * This structure is essentially a modified ConcurrentHashMap, in which each segment stores a hashtable mapping
	 * from keys to sets of values.
	 */

	/* ---------------- Constants -------------- */

	/** appease the serialization gods */
	private static final long serialVersionUID = -2408859344322772079L;

	/**
	 * The default initial capacity for this table,
	 * used when not otherwise specified in a constructor.
	 */
	private static final int DEFAULT_INITIAL_KEY_CAPACITY = 16;

	/**
	 * Default initial capacity per key, the number of values expected to be mapped to a single key. Assuming here only
	 * small depth in the multimap, as a tradeoff against lots of wasted space in the case where the multimap isn't much
	 * more than just a map.
	 */
	static final int DEFAULT_INITIAL_VALUES_CAPACITY_PER_KEY = 2;

	/**
	 * The default load factor for this table, used when not
	 * otherwise specified in a constructor.
	 */
	static final float DEFAULT_LOAD_FACTOR = 0.75f;

	/**
	 * The default concurrency level for this table, used when not
	 * otherwise specified in a constructor.
	 */
	private static final int DEFAULT_CONCURRENCY_LEVEL = 16;

	/**
	 * The maximum capacity, used if a higher value is implicitly
	 * specified by either of the constructors with arguments. MUST
	 * be a power of two <= 1<<30 to ensure that entries are indexable
	 * using ints.
	 */
	private static final int MAXIMUM_CAPACITY = 1 << 30;

	/**
	 * The maximum number of segments to allow; used to bound
	 * constructor arguments.
	 */
	private static final int MAX_SEGMENTS = 1 << 16; // slightly conservative

	/**
	 * Number of unsynchronized retries in size and containsValue
	 * methods before resorting to locking. This is used to avoid
	 * unbounded retries if tables undergo continuous modification
	 * which would make it impossible to obtain an accurate result.
	 */
	private static final int RETRIES_BEFORE_LOCK = 2;

	/* ---------------- Fields -------------- */
	/**
	 * Mask value for indexing into segments. The upper bits of a
	 * key's hash code are used to choose the segment.
	 */
	final int segmentMask;
	/**
	 * Shift value for indexing within segments.
	 */
	final int segmentShift;
	/**
	 * The segments, each of which is a specialized hash table
	 */
	final Segment<K, V>[] segments;
	transient Set<K> keySet;
	transient Set<Entry<K, Set<V>>> entrySet;
	transient Collection<V> values;

	/* ---------------- Small Utilities -------------- */
	/**
	 * Applies a supplemental hash function to a given hashCode, which
	 * defends against poor quality hash functions. This is critical
	 * because ConcurrentHashMultimap uses power-of-two length hash tables,
	 * that otherwise encounter collisions for hashCodes that do not
	 * differ in lower or upper bits.
	 */
	static int hash(int h) {
		// Spread bits to regularize both segment and index locations,
		// using variant of single-word Wang/Jenkins hash.
		h += (h << 15) ^ 0xffffcd7d;
		h ^= (h >>> 10);
		h += (h << 3);
		h ^= (h >>> 6);
		h += (h << 2) + (h << 14);
		return h ^ (h >>> 16);
	}
	/**
	 * Returns the segment that should be used for key with given hash
	 * 
	 * @param hash the hash code for the key
	 * @return the segment
	 */
	final Segment<K, V> segmentFor(int hash) {
		return segments[(hash >>> segmentShift) & segmentMask];
	}
	/**
	 * Creates a live-view wrapper of the values mapped to some key
	 */
	Set<V> wrapValuesCollection(final K key) {
		final int keyHashCode = hash(key.hashCode());
		return new ValuesCollectionWrapper(keyHashCode, key);
	}

	private final class AsMap extends AbstractMap<K, Collection<V>> {
		AsMap() {}
		
		@Override
		public Set<Entry<K, Collection<V>>> entrySet() {
			return new AbstractSet<Entry<K, Collection<V>>>() {
				@Override
				public Iterator<Entry<K, Collection<V>>> iterator() {
					return new AsMapEntryIterator();
				}

				@Override
				public int size() {
					return keySet.size();
				}
			};
		}
		@Override
		public boolean containsKey(Object key) {
			return Maps.safeContainsKey(this, key);
		}
		@Override
		public Collection<V> get(Object key) {
			if (key == null) {
				return null;
			}
			
			int hash = hash(key.hashCode());
			Set<V> values = segmentFor(hash).get(key, hash);
			
			if (values == null || values.isEmpty()) { // linearisation point
				/*
				 * either key is not a K, or there are no values at the moment. Either way return null according to
				 * spec on asMap() and this bug: http://code.google.com/p/guava-libraries/issues/detail?id=437
				 */
				return null;
			} else {
				// key is definitely a K, since some mapping was added with this key
				@SuppressWarnings("unchecked")
				K kkey = (K) key;
				return wrapValuesCollection(kkey);
			}
		}
	}
	
	/* ---------------- Inner Classes -------------- */
	/**
	 * ConcurrentHashMultimap list entry. Note that this is exported
	 * out as a user-visible Map.Entry, but that it does not support {@link #setValue(Collection) setValue}. Entries are
	 * live only
	 * through a single iteration, but may not see updates if part of
	 * the entry chain is cloned. This satisfies the caveats given in {@link ConcurrentHashMultimap#entries()}.
	 * 
	 * Because the value field is volatile, not final, it is legal wrt
	 * the Java Memory Model for an unsynchronized reader to see null
	 * instead of initial value when read via a data race. Although a
	 * reordering leading to this is not likely to ever actually
	 * occur, the Segment.readValueUnderLock method is used as a
	 * backup in case a null (pre-initialized) value is ever seen in
	 * an unsynchronized access method.
	 */
	static final class HashEntry<K, V> implements Entry<K, Collection<V>> {
		final K key;
		final int hash;
		volatile Set<V> value;

		/*
		 * Values-collection need to be able to be updated concurrently to readers iterating through them, since
		 * readers don't hold the locks.
		 */
		static <V> Set<V> createValuesCollection(V value) {
			Set<V> set = createValuesCollection();
			set.add(value);
			return set;
		}
		static <V> Set<V> createValuesCollection(Iterable<? extends V> c) {
			Set<V> set = createValuesCollection();
			Iterables.addAll(set, c);
			return set;
		}
		private static <V> Set<V> createValuesCollection() {
			return Collections.newSetFromMap(new ConcurrentHashMap<V, Boolean>(DEFAULT_INITIAL_VALUES_CAPACITY_PER_KEY,
					DEFAULT_LOAD_FACTOR, 1));
		}

		final HashEntry<K, V> next;

		HashEntry(K key, int hash, HashEntry<K, V> next, Set<V> value) {
			this.key = key;
			this.hash = hash;
			this.next = next;
			this.value = value;
		}
		HashEntry(K key, int hash, HashEntry<K, V> next, V value) {
			this(key, hash, next, createValuesCollection(value));
		}

		@Override
		public K getKey() {
			return key;
		}
		@Override
		public Set<V> getValue() {
			return value;
		}
		@Override
		public Collection<V> setValue(Collection<V> value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int hashCode() {
			return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
		}
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Map.Entry)) {
				return false;
			}
			Entry<?, ?> e = (Entry<?, ?>) obj;
			return equal(key, e.getKey()) && equal(value, e.getValue());
		}

		@SuppressWarnings("unchecked")
		static final <K, V> HashEntry<K, V>[] newArray(int i) {
			return new HashEntry[i];
		}
	}
	/**
	 * Segments are specialized versions of hash tables. This
	 * subclasses from ReentrantLock opportunistically, just to
	 * simplify some locking and avoid separate construction.
	 */
	static final class Segment<K, V> extends ReentrantLock implements Serializable {
		/*
		 * Segments maintain a table of entry lists that are ALWAYS
		 * kept in a consistent state, so can be read without locking.
		 * Next fields of nodes are immutable (final). All list
		 * additions are performed at the front of each bin. This
		 * makes it easy to check changes, and also fast to traverse.
		 * When nodes would otherwise be changed, new nodes are
		 * created to replace them. This works well for hash tables
		 * since the bin lists tend to be short. (The average length
		 * is less than two for the default load factor threshold.)
		 * 
		 * Read operations can thus proceed without locking, but rely
		 * on selected uses of volatiles to ensure that completed
		 * write operations performed by other threads are
		 * noticed. For most purposes, the "count" field, tracking the
		 * number of elements, serves as that volatile variable
		 * ensuring visibility. This is convenient because this field
		 * needs to be read in many read operations anyway:
		 * 
		 * - All (unsynchronized) read operations must first read the
		 * "count" field, and should not look at table entries if
		 * it is 0.
		 * 
		 * - All (synchronized) write operations should write to
		 * the "count" field after structurally changing any bin.
		 * The operations must not take any action that could even
		 * momentarily cause a concurrent read operation to see
		 * inconsistent data. This is made easier by the nature of
		 * the read operations in Map. For example, no operation
		 * can reveal that the table has grown but the threshold
		 * has not yet been updated, so there are no atomicity
		 * requirements for this with respect to reads.
		 * 
		 * As a guide, all critical volatile reads and writes to the
		 * count field are marked in code comments.
		 */

		private static final long serialVersionUID = 2249069246763182397L;
		
		/*
		 * All of the fields except for loadFactor are transient, since on deserialization they will take default
		 * values, after which each element will be added to the map sequentially.
		 */

		/**
		 * The number of elements in this segment's region, for size()
		 */
		transient volatile int elementCount;
		/**
		 * The number of hash buckets in this segment's region, for rehash thresholds.
		 */
		transient int hashEntryCount;

		/**
		 * Number of updates that alter the size of the table. This is
		 * used during bulk-read methods to make sure they see a
		 * consistent snapshot: If modCounts change during a traversal
		 * of segments computing size or checking containsValue, then
		 * we might have an inconsistent view of state so (usually)
		 * must retry.
		 */
		transient int modCount;

		/**
		 * The table is rehashed when its size exceeds this threshold.
		 * (The value of this field is always <tt>(int)(capacity *
		 * loadFactor)</tt>.)
		 */
		transient int threshold;

		/**
		 * The per-segment table.
		 */
		transient volatile HashEntry<K, V>[] table;

		/**
		 * The load factor for the hash table. Even though this value
		 * is same for all segments, it is replicated to avoid needing
		 * links to outer object.
		 * 
		 * @serial
		 */
		final float loadFactor;

		Segment(int initialCapacity, float lf) {
			loadFactor = lf;
			setTable(HashEntry.<K, V> newArray(initialCapacity));
		}

		@SuppressWarnings("unchecked")
		static final <K, V> Segment<K, V>[] newArray(int i) {
			return new Segment[i];
		}

		/**
		 * Sets table to new HashEntry array.
		 * Call only while holding lock or in constructor.
		 */
		void setTable(HashEntry<K, V>[] newTable) {
			threshold = (int) (newTable.length * loadFactor);
			table = newTable;
		}

		/**
		 * Returns properly casted first entry of bin for given hash.
		 */
		HashEntry<K, V> getFirst(int hash) {
			HashEntry<K, V>[] tab = table;
			return tab[hash & (tab.length - 1)];
		}

		/**
		 * Reads value field of an entry under lock. Called if value
		 * field ever appears to be null. This is possible only if a
		 * compiler happens to reorder a HashEntry initialization with
		 * its table assignment, which is legal under memory model
		 * but is not known ever to occur.
		 */
		Set<V> readValueUnderLock(HashEntry<K, V> e) {
			lock();
			try {
				return e.value;
			} finally {
				unlock();
			}
		}

		/* Specialized implementations of map methods */

		Set<V> get(Object key, int hash) {
			if (elementCount != 0) { // read-volatile
				HashEntry<K, V> e = getFirst(hash);
				while (e != null) {
					if (e.hash == hash && key.equals(e.key)) {
						Set<V> v = e.value;
						if (v != null)
							return v;
						return readValueUnderLock(e); // recheck
					}
					e = e.next;
				}
			}
			return null;
		}

		boolean containsKey(Object key, int hash) {
			if (elementCount != 0) { // read-volatile
				HashEntry<K, V> e = getFirst(hash);
				while (e != null) {
					if (e.hash == hash && key.equals(e.key))
						return true;
					e = e.next;
				}
			}
			return false;
		}

		boolean containsValue(Object value) {
			if (elementCount != 0) { // read-volatile
				HashEntry<K, V>[] tab = table;
				int len = tab.length;
				for (int i = 0; i < len; i++) {
					for (HashEntry<K, V> e = tab[i]; e != null; e = e.next) {
						Set<V> v = e.value;
						if (v == null) // recheck
							v = readValueUnderLock(e);
						if (v.contains(value))
							return true;
					}
				}
			}
			return false;
		}

		boolean replace(K key, int hash, Set<V> oldValue, Set<V> newValue) {
			lock();
			try {
				int c = elementCount;
				HashEntry<K, V> e = getFirst(hash);
				while (e != null && (e.hash != hash || !key.equals(e.key)))
					e = e.next;

				boolean replaced = false;
				if (e != null && oldValue.equals(e.value)) {
					replaced = true;
					e.value = HashEntry.createValuesCollection(newValue);
					elementCount = c - oldValue.size() + newValue.size();
				}
				// no prior mapping - don't need to write to count, haven't done anything
				return replaced;
			} finally {
				unlock();
			}
		}
		boolean replace(K key, int hash, V oldValue, V newValue) {
			lock();
			try {
				int c = elementCount;
				HashEntry<K, V> e = getFirst(hash);
				while (e != null && (e.hash != hash || !key.equals(e.key)))
					e = e.next;

				boolean replaced = false;
				if (e != null && e.value.remove(oldValue)) {
					replaced = true;
					e.value.add(newValue);
					elementCount = c; // write-volatile - necessary? CHM valuesCollection probably provides the barrier
				}
				// no prior mapping - don't need to write to count, haven't done anything
				return replaced;
			} finally {
				unlock();
			}
		}

		Set<V> replaceValues(K key, int hash, Iterable<? extends V> newVvalues) {
			lock();
			try {
				int c = elementCount;
				int incrementedHashEntryCount = hashEntryCount + 1;
				if (incrementedHashEntryCount > threshold) // ensure capacity
					rehash();

				final HashEntry<K, V>[] tab = table;
				final int index = hash & (tab.length - 1);
				final HashEntry<K, V> first = tab[index];

				HashEntry<K, V> e = first;
				while (e != null && (e.hash != hash || !key.equals(e.key))) {
					e = e.next;
				}

				Set<V> oldValues = null;
				if (e != null) {
					Set<V> valuesCollection = e.value;
					oldValues = ImmutableSet.copyOf(valuesCollection);
					valuesCollection.clear();
					Iterables.addAll(valuesCollection, newVvalues);
					elementCount = c - oldValues.size() + valuesCollection.size(); // write-volatile
					return oldValues;
				} else {
					// no prior mapping, just push the new values at the head of this entry chain
					Set<V> valuesCollection = HashEntry.createValuesCollection(newVvalues);
					if (!valuesCollection.isEmpty()) {
						tab[index] = new HashEntry<K, V>(key, hash, first, valuesCollection);
						hashEntryCount = incrementedHashEntryCount;
						elementCount = c + valuesCollection.size(); // write-volatile
					}
					// else no change, don't need a volatile write
					return null;
				}
			} finally {
				unlock();
			}
		}

		boolean put(K key, int hash, V value) {
			lock();
			try {
				int c = elementCount;
				int incrementedHashEntryCount = hashEntryCount + 1;
				if (incrementedHashEntryCount > threshold) // ensure capacity
					rehash();
				HashEntry<K, V>[] tab = table;
				int index = hash & (tab.length - 1);
				HashEntry<K, V> first = tab[index];
				HashEntry<K, V> e = first;
				while (e != null && (e.hash != hash || !key.equals(e.key)))
					e = e.next;

				Set<V> valuesCollection;
				if (e != null) {
					valuesCollection = e.value;
					boolean wasModified = valuesCollection.add(value);
					elementCount = wasModified ? c + 1 : c;
					return wasModified;
				} else {
					valuesCollection = null;
					++modCount;
					tab[index] = new HashEntry<K, V>(key, hash, first, value);
					hashEntryCount = incrementedHashEntryCount;
					elementCount = c + 1; // write-volatile
					return true;
				}
			} finally {
				unlock();
			}
		}
		boolean put(K key, int hash, Iterable<? extends V> values) {
			lock();
			try {
				int c = elementCount;
				int incrementedHashEntryCount = hashEntryCount + 1;
				if (incrementedHashEntryCount > threshold) // ensure capacity
					rehash();
				HashEntry<K, V>[] tab = table;
				int index = hash & (tab.length - 1);
				HashEntry<K, V> first = tab[index];
				HashEntry<K, V> e = first;
				while (e != null && (e.hash != hash || !key.equals(e.key)))
					e = e.next;

				Set<V> valuesCollection;
				++modCount;
				if (e != null) {
					valuesCollection = e.value;

					int priorSize = valuesCollection.size();
					boolean wasModified = Iterables.addAll(valuesCollection, values);
					elementCount = c - priorSize + valuesCollection.size(); // write-volatile
					return wasModified;
				} else {
					valuesCollection = HashEntry.createValuesCollection(values);
					tab[index] = new HashEntry<K, V>(key, hash, first, valuesCollection);
					hashEntryCount = incrementedHashEntryCount;
					elementCount = c + valuesCollection.size(); // write-volatile
					return true;
				}
			} finally {
				unlock();
			}
		}

		void rehash() {
			HashEntry<K, V>[] oldTable = table;
			int oldCapacity = oldTable.length;
			if (oldCapacity >= MAXIMUM_CAPACITY)
				return;

			/*
			 * Reclassify nodes in each list to new Map. Because we are
			 * using power-of-two expansion, the elements from each bin
			 * must either stay at same index, or move with a power of two
			 * offset. We eliminate unnecessary node creation by catching
			 * cases where old nodes can be reused because their next
			 * fields won't change. Statistically, at the default
			 * threshold, only about one-sixth of them need cloning when
			 * a table doubles. The nodes they replace will be garbage
			 * collectable as soon as they are no longer referenced by any
			 * reader thread that may be in the midst of traversing table
			 * right now.
			 */

			HashEntry<K, V>[] newTable = HashEntry.newArray(oldCapacity << 1);
			threshold = (int) (newTable.length * loadFactor);
			int sizeMask = newTable.length - 1;
			for (int i = 0; i < oldCapacity; i++) {
				// We need to guarantee that any existing reads of old Map can
				// proceed. So we cannot yet null out each bin.
				HashEntry<K, V> e = oldTable[i];

				if (e != null) {
					HashEntry<K, V> next = e.next;
					int idx = e.hash & sizeMask;

					// Single node on list
					if (next == null)
						newTable[idx] = e;

					else {
						// Reuse trailing consecutive sequence at same slot
						HashEntry<K, V> lastRun = e;
						int lastIdx = idx;
						for (HashEntry<K, V> last = next; last != null; last = last.next) {
							int k = last.hash & sizeMask;
							if (k != lastIdx) {
								lastIdx = k;
								lastRun = last;
							}
						}
						newTable[lastIdx] = lastRun;

						// Clone all remaining nodes
						for (HashEntry<K, V> p = e; p != lastRun; p = p.next) {
							int k = p.hash & sizeMask;
							HashEntry<K, V> n = newTable[k];
							newTable[k] = new HashEntry<K, V>(p.key, p.hash, n, p.value);
						}
					}
				}
			}
			table = newTable;
		}

		/**
		 * Remove; match on key only if value null, else match both.
		 */
		boolean remove(Object key, int hash, Object value) {
			lock();
			try {
				int count = elementCount;
				HashEntry<K, V>[] tab = table;
				int index = hash & (tab.length - 1);
				HashEntry<K, V> first = tab[index];
				HashEntry<K, V> e = first;
				while (e != null && (e.hash != hash || !key.equals(e.key)))
					e = e.next;

				if (e != null) {
					Set<V> v = e.value;

					/*
					 * value == null means removeAll(key)
					 * v == {value} effectively means removeAll(key)
					 * |v| > 1 means just remove(key, value)
					 */

					boolean entryContainsValue = v.contains(value);
					if (!entryContainsValue) {
						return false;
					}
					boolean removeEntry = value == null || (v.size() == 1 && entryContainsValue);

					if (removeEntry) {
						removeHashEntry(hash, tab, first, e); // updates hashEntryCount
					} else {
						v.remove(value);
					}
					elementCount = count - 1; // write-volatile
					return true;
				} else {
					// don't need to write to count, haven't done anything
					return false;
				}
			} finally {
				unlock();
			}
		}

		/**
		 * Removes a HashEntry from the chain. This will change the hashEntryCount, but not the element count.
		 */
		void removeHashEntry(int hash, HashEntry<K, V>[] tab, HashEntry<K, V> first, HashEntry<K, V> entryToRemove) {
			assert this.isHeldByCurrentThread();

			int indexOfEntry = hash & (tab.length - 1);
			// All entries following removed node can stay
			// in list, but all preceding ones need to be
			// cloned.
			++modCount;
			HashEntry<K, V> newFirst = entryToRemove.next;
			for (HashEntry<K, V> p = first; p != entryToRemove; p = p.next)
				newFirst = new HashEntry<K, V>(p.key, p.hash, newFirst, p.value);
			tab[indexOfEntry] = newFirst;
			--hashEntryCount;
		}

		Set<V> removeAll(Object key, int hash) {
			lock();
			try {
				final int initialCount = elementCount;
				HashEntry<K, V>[] tab = table;
				int index = hash & (tab.length - 1);
				HashEntry<K, V> first = tab[index];
				HashEntry<K, V> e = first;
				while (e != null && (e.hash != hash || !key.equals(e.key)))
					e = e.next;

				if (e != null) {
					Set<V> v = e.value;

					removeHashEntry(hash, tab, first, e); // updates hashEntryCount

					ImmutableSet<V> toReturn = ImmutableSet.copyOf(v);
					v.clear();
					elementCount = initialCount - toReturn.size(); // write-volatile
					return toReturn;
				}
				// no need to write to count, we haven't done anything
				return ImmutableSet.of();
			} finally {
				unlock();
			}
		}

		void clear() {
			if (elementCount != 0) {
				lock();
				try {
					HashEntry<K, V>[] tab = table;
					for (int i = 0; i < tab.length; i++) {
						HashEntry<K, V> hashEntry = tab[i];
						if (hashEntry != null) {
							hashEntry.value.clear(); // so that views are updated
							tab[i] = null;
						}
					}
					++modCount;
					hashEntryCount = 0;
					elementCount = 0; // write-volatile
				} finally {
					unlock();
				}
			}
		}
	}

	/* ---------------- Public operations -------------- */
	/**
	 * Creates a new, empty map with the specified initial
	 * capacity, load factor and concurrency level.
	 * 
	 * @param initialKeyCapacity the initial capacity. The implementation
	 *            performs internal sizing to accommodate this many elements.
	 * @param loadFactor the load factor threshold, used to control resizing.
	 *            Resizing may be performed when the average number of elements per
	 *            bin exceeds this threshold.
	 * @param concurrencyLevel the estimated number of concurrently
	 *            updating threads. The implementation performs internal sizing
	 *            to try to accommodate this many threads.
	 * @throws IllegalArgumentException if the initial capacity is
	 *             negative or the load factor or concurrencyLevel are
	 *             nonpositive.
	 */
	private ConcurrentHashMultimap(int initialKeyCapacity, int initialValuesCapacity, float loadFactor,
			int concurrencyLevel) {
		if (!(loadFactor > 0) || initialKeyCapacity < 0 || concurrencyLevel <= 0)
			throw new IllegalArgumentException();

		if (concurrencyLevel > MAX_SEGMENTS)
			concurrencyLevel = MAX_SEGMENTS;

		// Find power-of-two sizes best matching arguments
		int sshift = 0;
		int ssize = 1;
		while (ssize < concurrencyLevel) {
			++sshift;
			ssize <<= 1;
		}
		segmentShift = 32 - sshift;
		segmentMask = ssize - 1;
		this.segments = Segment.newArray(ssize);

		if (initialKeyCapacity > MAXIMUM_CAPACITY)
			initialKeyCapacity = MAXIMUM_CAPACITY;
		int c = initialKeyCapacity / ssize;
		if (c * ssize < initialKeyCapacity)
			++c;
		int cap = 1;
		while (cap < c)
			cap <<= 1;

		for (int i = 0; i < this.segments.length; ++i)
			this.segments[i] = new Segment<K, V>(cap, loadFactor);
	}
	/**
	 * Creates a new, empty map with the specified initial capacity
	 * and load factor and with the default concurrencyLevel (16).
	 * 
	 * @param initialCapacity The implementation performs internal
	 *            sizing to accommodate this many elements.
	 * @param loadFactor the load factor threshold, used to control resizing.
	 *            Resizing may be performed when the average number of elements per
	 *            bin exceeds this threshold.
	 * @throws IllegalArgumentException if the initial capacity of
	 *             elements is negative or the load factor is nonpositive
	 * 
	 * @since 1.6
	 */
	private ConcurrentHashMultimap(int initialCapacity, float loadFactor) {
		this(initialCapacity, DEFAULT_INITIAL_VALUES_CAPACITY_PER_KEY, loadFactor, DEFAULT_CONCURRENCY_LEVEL);
	}
	/**
	 * Creates a new, empty map with the specified initial capacity,
	 * and with default load factor (0.75) and concurrencyLevel (16).
	 * 
	 * @param initialCapacity the initial capacity. The implementation
	 *            performs internal sizing to accommodate this many elements.
	 * @throws IllegalArgumentException if the initial capacity of
	 *             elements is negative.
	 */
	private ConcurrentHashMultimap(int initialCapacity) {
		this(initialCapacity, DEFAULT_INITIAL_VALUES_CAPACITY_PER_KEY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL);
	}
	/**
	 * Creates a new, empty map with a default initial capacity (16),
	 * load factor (0.75) and concurrencyLevel (16).
	 */
	private ConcurrentHashMultimap() {
		this(DEFAULT_INITIAL_KEY_CAPACITY, DEFAULT_INITIAL_VALUES_CAPACITY_PER_KEY, DEFAULT_LOAD_FACTOR,
				DEFAULT_CONCURRENCY_LEVEL);
	}
	/**
	 * Creates a new map with the same mappings as the given map.
	 * The map is created with a capacity of 1.5 times the number
	 * of mappings in the given map or 16 (whichever is greater),
	 * and a default load factor (0.75) and concurrencyLevel (16).
	 * 
	 * @param m the map
	 */
	private ConcurrentHashMultimap(Multimap<? extends K, ? extends V> m) {
		this(Math.max((int) (m.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_INITIAL_KEY_CAPACITY),
				DEFAULT_INITIAL_VALUES_CAPACITY_PER_KEY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL);
		putAll(m);
	}

	public static <K, V> ConcurrentHashMultimap<K, V> create() {
		return new ConcurrentHashMultimap<K, V>();
	}
	public static <K, V> ConcurrentHashMultimap<K, V> create(int initialKeyCapacity) {
		return new ConcurrentHashMultimap<K, V>(initialKeyCapacity);
	}
	public static <K, V> ConcurrentHashMultimap<K, V> create(int initialKeyCapacity, int initialValuesPerKeyCapacity,
			float loadFactor, int concurrencyLevel) {
		return new ConcurrentHashMultimap<K, V>(initialKeyCapacity, initialValuesPerKeyCapacity, loadFactor,
				concurrencyLevel);
	}
	public static <K, V> ConcurrentHashMultimap<K, V> create(Multimap<? extends K, ? extends V> multimap) {
		return new ConcurrentHashMultimap<K, V>(multimap);
	}

	@Override
	public boolean isEmpty() {
		final Segment<K, V>[] segments = this.segments;
		/*
		 * We keep track of per-segment modCounts to avoid ABA
		 * problems in which an element in one segment was added and
		 * in another removed during traversal, in which case the
		 * table was never actually empty at any point. Note the
		 * similar use of modCounts in the size() and containsValue()
		 * methods, which are the only other methods also susceptible
		 * to ABA problems.
		 */
		int[] mc = new int[segments.length];
		int mcsum = 0;
		for (int i = 0; i < segments.length; ++i) {
			if (segments[i].elementCount != 0)
				return false;
			else
				mcsum += mc[i] = segments[i].modCount;
		}
		// If mcsum happens to be zero, then we know we got a snapshot
		// before any modifications at all were made. This is
		// probably common enough to bother tracking.
		if (mcsum != 0) {
			for (int i = 0; i < segments.length; ++i) {
				if (segments[i].elementCount != 0 || mc[i] != segments[i].modCount)
					return false;
			}
		}
		return true;
	}
	@Override
	public int size() {
		final Segment<K, V>[] segments = this.segments;
		long sum = 0;
		long check = 0;
		int[] mc = new int[segments.length];
		// Try a few times to get accurate count. On failure due to
		// continuous async changes in table, resort to locking.
		for (int k = 0; k < RETRIES_BEFORE_LOCK; ++k) {
			check = 0;
			sum = 0;
			int mcsum = 0;
			for (int i = 0; i < segments.length; ++i) {
				sum += segments[i].elementCount;
				mcsum += mc[i] = segments[i].modCount;
			}
			if (mcsum != 0) {
				for (int i = 0; i < segments.length; ++i) {
					check += segments[i].elementCount;
					if (mc[i] != segments[i].modCount) {
						check = -1; // force retry
						break;
					}
				}
			}
			if (check == sum)
				break;
		}
		if (check != sum) { // Resort to locking all segments
			sum = 0;
			for (int i = 0; i < segments.length; ++i)
				segments[i].lock();
			for (int i = 0; i < segments.length; ++i)
				sum += segments[i].elementCount;
			for (int i = 0; i < segments.length; ++i)
				segments[i].unlock();
		}
		if (sum > Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		else
			return (int) sum;
	}
	int keyCount() {
		final Segment<K, V>[] segments = this.segments;
		long sum = 0;
		long check = 0;
		int[] mc = new int[segments.length];
		// Try a few times to get accurate count. On failure due to
		// continuous async changes in table, resort to locking.
		for (int k = 0; k < RETRIES_BEFORE_LOCK; ++k) {
			check = 0;
			sum = 0;
			int mcsum = 0;
			for (int i = 0; i < segments.length; ++i) {
				sum += segments[i].hashEntryCount;
				mcsum += mc[i] = segments[i].modCount;
			}
			if (mcsum != 0) {
				for (int i = 0; i < segments.length; ++i) {
					check += segments[i].hashEntryCount;
					if (mc[i] != segments[i].modCount) {
						check = -1; // force retry
						break;
					}
				}
			}
			if (check == sum)
				break;
		}
		if (check != sum) { // Resort to locking all segments
			sum = 0;
			for (int i = 0; i < segments.length; ++i)
				segments[i].lock();
			for (int i = 0; i < segments.length; ++i)
				sum += segments[i].hashEntryCount;
			for (int i = 0; i < segments.length; ++i)
				segments[i].unlock();
		}
		if (sum > Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		else
			return (int) sum;
	}
	@Override
	public Set<V> get(K key) {
		return wrapValuesCollection(key);
	}
	@Override
	public boolean containsKey(Object key) {
		int hash = hash(key.hashCode());
		return segmentFor(hash).containsKey(key, hash);
	}
	@Override
	public boolean containsValue(Object value) {
		if (value == null)
			throw new NullPointerException();

		// See explanation of modCount use above

		final Segment<K, V>[] segments = this.segments;
		int[] mc = new int[segments.length];

		// Try a few times without locking
		for (int k = 0; k < RETRIES_BEFORE_LOCK; ++k) {
			int mcsum = 0;
			for (int i = 0; i < segments.length; ++i) {
				@SuppressWarnings("unused")
				int c = segments[i].elementCount; // read-volatile
				mcsum += mc[i] = segments[i].modCount;
				if (segments[i].containsValue(value))
					return true;
			}
			boolean cleanSweep = true;
			if (mcsum != 0) {
				for (int i = 0; i < segments.length; ++i) {
					@SuppressWarnings("unused")
					int c = segments[i].elementCount; // read-volatile
					if (mc[i] != segments[i].modCount) {
						cleanSweep = false;
						break;
					}
				}
			}
			if (cleanSweep)
				return false;
		}
		// Resort to locking all segments
		for (int i = 0; i < segments.length; ++i)
			segments[i].lock();
		boolean found = false;
		try {
			for (int i = 0; i < segments.length; ++i) {
				if (segments[i].containsValue(value)) {
					found = true;
					break;
				}
			}
		} finally {
			for (int i = 0; i < segments.length; ++i)
				segments[i].unlock();
		}
		return found;
	}
	@Override
	public boolean containsEntry(Object key, Object value) {
		int hash = hash(key.hashCode());
		Set<V> values = segmentFor(hash).get(key, hash);
		return values != null && values.contains(value);
	}
	@Override
	public boolean put(K key, V value) {
		if (value == null)
			throw new NullPointerException();
		int hash = hash(key.hashCode());
		return segmentFor(hash).put(key, hash, value);
	}
	@Override
	public boolean putAll(Multimap<? extends K, ? extends V> m) {
		boolean changed = false;
		for (Entry<? extends K, ? extends V> entry : m.entries()) {
			changed |= put(entry.getKey(), entry.getValue());
		}
		return changed;
	}
	@Override
	public boolean putAll(K key, Iterable<? extends V> values) {
		int hash = hash(key.hashCode());
		return segmentFor(hash).put(key, hash, values);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws NullPointerException if the specified key is null
	 */
	@Override
	public boolean remove(Object key, Object value) {
		int hash = hash(key.hashCode());
		if (value == null)
			return false;
		return segmentFor(hash).remove(key, hash, value);
	}
	@Override
	public Set<V> removeAll(Object key) {
		int hash = hash(key.hashCode());
		return segmentFor(hash).removeAll(key, hash);
	}
	/**
	 * Removes all of the mappings from this map.
	 */
	@Override
	public void clear() {
		for (int i = 0; i < segments.length; ++i)
			segments[i].clear();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws NullPointerException if any of the arguments are null
	 */
	@Override
	public boolean replaceValue(K key, V oldValue, V newValue) {
		if (oldValue == null || newValue == null)
			throw new NullPointerException();
		int hash = hash(key.hashCode());
		return segmentFor(hash).replace(key, hash, oldValue, newValue);
	}
	/**
	 * {@inheritDoc}
	 * 
	 * @return the previous value associated with the specified key,
	 *         or <tt>null</tt> if there was no mapping for the key
	 * @throws NullPointerException if the specified key or value is null
	 */
	@Override
	public Set<V> replaceValues(K key, Iterable<? extends V> values) {
		if (values == null)
			throw new NullPointerException();
		int hash = hash(key.hashCode());
		return segmentFor(hash).replaceValues(key, hash, values);
	}

	@Override
	public Set<K> keySet() {
		Set<K> ks = keySet;
		return (ks != null) ? ks : (keySet = new DistinctKeySet());
	}
	@Override
	public Collection<V> values() {
		Collection<V> vs = values;
		return (vs != null) ? vs : (values = new Values());
	}
	@Override
	public Map<K, Collection<V>> asMap() {
		return new AsMap();
	}
	@Override
	public Multiset<K> keys() {
		return new AbstractMultiset<K>() {
			@Override
			int distinctElements() {
				return ConcurrentHashMultimap.this.keyCount();
			}
			@Override
			Iterator<Entry<K>> entryIterator() {
				return Iterators.transform(keySet().iterator(), sizeForKey);
			}
		};
	}
	transient final Function<K, Multiset.Entry<K>> sizeForKey = new Function<K, Multiset.Entry<K>>() {
		@Override
		public Multiset.Entry<K> apply(final K input) {
			return new Multisets.AbstractEntry<K>() {
				@Override
				public K getElement() {
					return input;
				}
				@Override
				public int getCount() {
					return get(input).size();
				}
			};
		};
	};
	/**
	 * {@inheritDoc} Entries in this set do not support the {@link Entry#setValue(Object) setValue} operation, since
	 * this would allow the unique values condition to be violated.
	 * <p>
	 * The view's <tt>iterator</tt> is a "weakly consistent" iterator that will never throw
	 * {@link ConcurrentModificationException}, and guarantees to traverse elements as they existed upon construction of
	 * the iterator, and may (but is not guaranteed to) reflect any modifications subsequent to construction.
	 */
	@Override
	public Set<Entry<K, V>> entries() {
		return new AbstractSet<Entry<K, V>>() {
			@Override
			public Iterator<Entry<K, V>> iterator() {
				return new EntryIterator();
			}
			@Override
			public int size() {
				return ConcurrentHashMultimap.this.size();
			}
			@Override
			public void clear() {
				ConcurrentHashMultimap.this.clear();
			}
			@Override
			public boolean contains(Object o) {
				if (o instanceof Entry) {
					Entry<?, ?> entry = (Entry<?, ?>) o;
					return ConcurrentHashMultimap.this.containsEntry(entry.getKey(), entry.getValue());
				}
				return false;
			}
		};
	}

	// Comparison and hashing

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		if (!(o instanceof Multimap)) {
			return false;
		}

		@SuppressWarnings("unchecked")
		Multimap<K, V> that = (Multimap<K, V>) o;
		if (that.size() != size()) {
			return false;
		}

		try {
			Iterator<Entry<K, Collection<V>>> i = asMap().entrySet().iterator();
			while (i.hasNext()) {
				Entry<K, Collection<V>> e = i.next();
				K key = e.getKey();
				Collection<V> value = e.getValue();
				if (value == null) {
					if (!(that.get(key) == null && that.containsKey(key))) {
						return false;
					}
				} else {
					if (!value.equals(that.get(key))) {
						return false;
					}
				}
			}
		} catch (ClassCastException unused) {
			return false;
		} catch (NullPointerException unused) {
			return false;
		}

		return true;
	}
	@Override
	public int hashCode() {
		int h = 0;
		Iterator<Entry<K, Collection<V>>> i = asMap().entrySet().iterator();
		while (i.hasNext())
			h += i.next().hashCode();
		return h;
	}
	@Override
	public String toString() {
		Iterator<Entry<K, Collection<V>>> i = asMap().entrySet().iterator();
		if (!i.hasNext())
			return "{}";

		StringBuilder sb = new StringBuilder();
		sb.append('{');
		for (;;) {
			Entry<K, Collection<V>> e = i.next();
			K key = e.getKey();
			Collection<V> value = e.getValue();
			sb.append(key == this ? "(this Map)" : key);
			sb.append('=');
			sb.append(value);
			if (!i.hasNext())
				return sb.append('}').toString();
			sb.append(", ");
		}
	}
	/* ---------------- Iterator Support -------------- */
	abstract class HashIterator {
		int nextSegmentIndex;
		int nextTableIndex;
		HashEntry<K, V>[] currentTable;
		HashEntry<K, V> nextEntry;
		HashEntry<K, V> lastReturned;

		HashIterator() {
			nextSegmentIndex = segments.length - 1;
			nextTableIndex = -1;
			advance();
		}

		public boolean hasMoreElements() {
			return hasNext();
		}

		final void advance() {
			if (nextEntry != null && (nextEntry = nextEntry.next) != null)
				return;

			while (nextTableIndex >= 0) {
				if ((nextEntry = currentTable[nextTableIndex--]) != null)
					return;
			}

			while (nextSegmentIndex >= 0) {
				Segment<K, V> seg = segments[nextSegmentIndex--];
				if (seg.elementCount != 0) {
					currentTable = seg.table;
					for (int j = currentTable.length - 1; j >= 0; --j) {
						if ((nextEntry = currentTable[j]) != null) {
							nextTableIndex = j - 1;
							return;
						}
					}
				}
			}
		}

		public boolean hasNext() {
			return nextEntry != null;
		}

		HashEntry<K, V> nextHashEntry() {
			if (nextEntry == null)
				throw new NoSuchElementException();
			lastReturned = nextEntry;
			advance();
			return lastReturned;
		}
	}
	class AsMapEntryIterator extends HashIterator implements Iterator<Entry<K, Collection<V>>> {
		@Override
		public Entry<K, Collection<V>> next() {
			removed = false;
			return nextHashEntry();
		}
		boolean removed = false;
		@Override
		public void remove() {
			checkState(!removed, "Element removed since next() was last called");
			ConcurrentHashMultimap.this.removeAll(lastReturned.getKey());
			removed = true;
		}
	}
	class EntryIteratorSupport extends HashIterator {
		private Iterator<V> valuesIterator;
		private Entry<K, V> lastReturnedEntry;

		Entry<K, V> nextEntry() {
			if (valuesIterator == null || !valuesIterator.hasNext()) {
				valuesIterator = super.nextHashEntry().value.iterator(); // throws if nothing left
			}
			lastReturnedEntry = Maps.immutableEntry(lastReturned.key, valuesIterator.next());
			return lastReturnedEntry;
		}
		@Override
		public boolean hasNext() {
			return (valuesIterator != null && valuesIterator.hasNext())
					|| (super.hasNext() && !nextEntry.value.isEmpty());
		}
		public void remove() {
			checkState(lastReturnedEntry != null);
			ConcurrentHashMultimap.this.remove(lastReturnedEntry.getKey(), lastReturnedEntry.getValue());
		}
	}
	/**
	 * Iterator through distinct keys.
	 * 
	 * @author Joe Kearney
	 */
	final class DistinctKeyIterator extends HashIterator implements Iterator<K> {
		@Override
		public K next() {
			return super.nextHashEntry().key;
		}
		@Override
		public void remove() {
			checkState(lastReturned != null);
			ConcurrentHashMultimap.this.removeAll(lastReturned.key);
		}
	}
	final class ValueIterator extends EntryIteratorSupport implements Iterator<V> {
		@Override
		public V next() {
			return super.nextEntry().getValue();
		}
	}
	final class EntryIterator extends EntryIteratorSupport implements Iterator<Entry<K, V>> {
		@Override
		public Entry<K, V> next() {
			return nextEntry();
		}
	}
	final class DistinctKeySet extends AbstractSet<K> {
		@Override
		public Iterator<K> iterator() {
			return new DistinctKeyIterator();
		}
		@Override
		public int size() {
			return ConcurrentHashMultimap.this.keyCount();
		}
		@Override
		public boolean contains(Object o) {
			return ConcurrentHashMultimap.this.containsKey(o);
		}
		@Override
		public boolean remove(Object o) {
			return ConcurrentHashMultimap.this.removeAll(o) != null;
		}
		@Override
		public void clear() {
			ConcurrentHashMultimap.this.clear();
		}
	}
	final class Values extends AbstractCollection<V> {
		@Override
		public Iterator<V> iterator() {
			return new ValueIterator();
		}
		@Override
		public int size() {
			return ConcurrentHashMultimap.this.size();
		}
		@Override
		public boolean contains(Object o) {
			return ConcurrentHashMultimap.this.containsValue(o);
		}
		@Override
		public void clear() {
			ConcurrentHashMultimap.this.clear();
		}
	}
	final class EntrySet extends AbstractSet<Entry<K, V>> {
		@Override
		public Iterator<Entry<K, V>> iterator() {
			return new EntryIterator();
		}
		@Override
		public boolean contains(Object o) {
			if (!(o instanceof Map.Entry))
				return false;
			Entry<?, ?> e = (Entry<?, ?>) o;
			return ConcurrentHashMultimap.this.containsEntry(e.getKey(), e.getValue());
		}
		@Override
		public boolean remove(Object o) {
			if (!(o instanceof Map.Entry))
				return false;
			Entry<?, ?> e = (Entry<?, ?>) o;
			return ConcurrentHashMultimap.this.remove(e.getKey(), e.getValue());
		}
		@Override
		public int size() {
			return ConcurrentHashMultimap.this.size();
		}
		@Override
		public void clear() {
			ConcurrentHashMultimap.this.clear();
		}
	}
	final class ValuesCollectionWrapper extends AbstractSet<V> {
		private final int keyHashCode;
		private final K key;

		ValuesCollectionWrapper(int keyHashCode, K key) {
			this.keyHashCode = keyHashCode;
			this.key = key;
		}

		@Override
		public boolean remove(Object object) {
			return ConcurrentHashMultimap.this.remove(key, object);
		}
		@Override
		public void clear() {
			ConcurrentHashMultimap.this.removeAll(key);
		}
		@Override
		public boolean add(V value) {
			return ConcurrentHashMultimap.this.put(key, value);
		}
		@Override
		public boolean addAll(Collection<? extends V> c) {
			return ConcurrentHashMultimap.this.putAll(key, c);
		}
		@Override
		public boolean contains(Object value) {
			return ConcurrentHashMultimap.this.containsEntry(key, value);
		}
		@Override
		public Iterator<V> iterator() {
			// go to the real set
			Set<V> set = segmentFor(keyHashCode).get(key, keyHashCode);
			return set == null ? Iterators.<V> emptyIterator() : set.iterator();
		}
		@Override
		public int size() {
			Set<V> set = segmentFor(keyHashCode).get(key, keyHashCode);
			return set == null ? 0 : set.size();
		}
	}

	/* ---------------- Serialization Support -------------- */
	/**
	 * Save the state of the <tt>ConcurrentHashMultimap</tt> instance to a
	 * stream (i.e., serialize it).
	 * 
	 * @param s the stream
	 * @serialData
	 *             the key (Object) and value (Object)
	 *             for each key-value mapping, followed by a null pair.
	 *             The key-value mappings are emitted in no particular order.
	 */
	private void writeObject(java.io.ObjectOutputStream s) throws IOException {
		s.defaultWriteObject();

		for (int k = 0; k < segments.length; ++k) {
			Segment<K, V> seg = segments[k];
			seg.lock();
			try {
				HashEntry<K, V>[] tab = seg.table;
				for (int i = 0; i < tab.length; ++i) {
					for (HashEntry<K, V> e = tab[i]; e != null; e = e.next) {
						Set<V> valuesCollection = e.value;
						for (V values : valuesCollection) {
							s.writeObject(e.key);
							s.writeObject(values);
						}
					}
				}
			} finally {
				seg.unlock();
			}
		}
		s.writeObject(null);
		s.writeObject(null);
	}
	/**
	 * Reconstitute the <tt>ConcurrentHashMultimap</tt> instance from a
	 * stream (i.e., deserialize it).
	 * 
	 * @param s the stream
	 */
	@SuppressWarnings("unchecked")
	private void readObject(java.io.ObjectInputStream s) throws IOException, ClassNotFoundException {
		s.defaultReadObject();

		// Initialize each segment to be minimally sized, and let grow.
		for (int i = 0; i < segments.length; ++i) {
			segments[i].setTable(new HashEntry[1]);
		}

		// Read the keys and values, and put the mappings in the table
		for (;;) {
			K key = (K) s.readObject();
			V value = (V) s.readObject();
			if (key == null)
				break;
			put(key, value);
		}
	}
}
