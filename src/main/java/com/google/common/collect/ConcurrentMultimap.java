package com.google.common.collect;

public interface ConcurrentMultimap<K, V> extends Multimap<K, V> {
	/**
	 * Replaces the entry for a key only if currently mapped to a given value. This is equivalent to
	 * 
	 * <pre>
     *   if (mmap.containsKey(key) &amp;&amp; mmap.get(key).contains(oldValue)) {
     *       mmap.remove(key, oldValue);
     *       mmap.put(key, newValue);
     *       return true;
     *   } else return false;</pre>
	 * 
	 * except that the action is performed atomically.
	 * 
	 * @param key key with which the specified value is associated
	 * @param oldValue value expected to be associated with the specified key
	 * @param newValue value to be associated with the specified key
	 * @return <tt>true</tt> if the value was replaced
	 * @throws UnsupportedOperationException if the <tt>put</tt> operation is not supported by this map
	 * @throws ClassCastException if the class of a specified key or value prevents it from being stored in this map
	 * @throws NullPointerException if a specified key or value is null, and this map does not permit null keys or
	 *             values
	 * @throws IllegalArgumentException if some property of a specified key or value prevents it from being stored in
	 *             this map
	 */
	boolean replaceValue(K key, V oldValue, V newValue);
}
