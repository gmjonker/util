/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Borrowed from Solr.
 */

//package org.apache.solr.util;
package gmjonker.util;

import com.google.common.collect.BoundType;
import com.google.common.collect.Multiset;
import com.google.common.collect.SortedMultiset;
import com.google.common.collect.TreeMultiset;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A TreeMultiset that ensures it never grows beyond a max size.  
 * <code>last()</code> is removed if the <code>size()</code> 
 * get's bigger then <code>getMaxSize()</code>
 */
public class BoundedTreeMultiset<E extends Comparable> implements SortedMultiset<E> 
{
    private final TreeMultiset<E> treeMultiset;// = TreeMultiset.create();

    private int maxSize = Integer.MAX_VALUE;

    public BoundedTreeMultiset(int maxSize)
    {
        treeMultiset = create();
        this.setMaxSize(maxSize);
    }

    public BoundedTreeMultiset(int maxSize, Collection<? extends E> coll)
    {
        treeMultiset = create(coll);
        this.setMaxSize(maxSize);
    }

    public BoundedTreeMultiset(int maxSize, Comparator<? super E> comp)
    {
        treeMultiset = create(comp);
        this.setMaxSize(maxSize);
    }

    public BoundedTreeMultiset(int maxSize, SortedSet<E> set)
    {
        treeMultiset = create(set);
        this.setMaxSize(maxSize);
    }

    public int getMaxSize()
    {
        return maxSize;
    }

    public void setMaxSize(int max)
    {
        maxSize = max;
        adjust();
    }

    private void adjust()
    {
        while (maxSize < size()) {
            int size = size();
            remove(lastEntry().getElement());
            assert size() < size : "Oops, BoundedTreeMultiset didn't get smaller after removal! -- " + treeMultiset;
        }
    }

    public boolean add(E item)
    {
        boolean out = _add(item);
        adjust();
        return out;
    }

    public boolean addAll(Collection<? extends E> c)
    {
        boolean out = _addAll(c);
        adjust();
        return out;
    }

    @Override
    public String toString()
    {
        return "BoundedTreeMultiset{" +
                "backing set=" + treeMultiset +
                ", maxSize=" + maxSize +
                '}';
    }
    
    //
    // Delegate methods
    // 
    
    public static <E1 extends Comparable> TreeMultiset<E1> create()
    {
        return TreeMultiset.create();
    }

    public static <E1> TreeMultiset<E1> create(@Nullable Comparator<? super E1> comparator)
    {
        return TreeMultiset.create(comparator);
    }

    public static <E1 extends Comparable> TreeMultiset<E1> create(Iterable<? extends E1> elements)
    {
        return TreeMultiset.create(elements);
    }

    public int size()
    {
        return treeMultiset.size();
    }

    public int count(@Nullable Object element)
    {
        return treeMultiset.count(element);
    }

    public int add(@Nullable E element, int occurrences)
    {
        return treeMultiset.add(element, occurrences);
    }

    public int remove(@Nullable Object element, int occurrences)
    {
        return treeMultiset.remove(element, occurrences);
    }

    public int setCount(@Nullable E element, int count)
    {
        return treeMultiset.setCount(element, count);
    }

    public boolean setCount(@Nullable E element, int oldCount, int newCount)
    {
        return treeMultiset.setCount(element, oldCount, newCount);
    }

    public SortedMultiset<E> headMultiset(@Nullable E upperBound,
            BoundType boundType)
    {
        return treeMultiset.headMultiset(upperBound, boundType);
    }

    public SortedMultiset<E> tailMultiset(@Nullable E lowerBound,
            BoundType boundType)
    {
        return treeMultiset.tailMultiset(lowerBound, boundType);
    }

    public NavigableSet<E> elementSet()
    {
        return treeMultiset.elementSet();
    }

    public Comparator<? super E> comparator()
    {
        return treeMultiset.comparator();
    }

    public Multiset.Entry<E> firstEntry()
    {
        return treeMultiset.firstEntry();
    }

    public Multiset.Entry<E> lastEntry()
    {
        return treeMultiset.lastEntry();
    }

    public Multiset.Entry<E> pollFirstEntry()
    {
        return treeMultiset.pollFirstEntry();
    }

    public Multiset.Entry<E> pollLastEntry()
    {
        return treeMultiset.pollLastEntry();
    }

    public SortedMultiset<E> subMultiset(@Nullable E fromElement,
            BoundType fromBoundType, @Nullable E toElement,
            BoundType toBoundType)
    {
        return treeMultiset.subMultiset(fromElement, fromBoundType, toElement, toBoundType);
    }

    public SortedMultiset<E> descendingMultiset()
    {
        return treeMultiset.descendingMultiset();
    }

    public boolean isEmpty()
    {
        return treeMultiset.isEmpty();
    }

    public boolean contains(@Nullable Object element)
    {
        return treeMultiset.contains(element);
    }

    public Iterator<E> iterator()
    {
        return treeMultiset.iterator();
    }

    public boolean _add(@Nullable E element)
    {
        return treeMultiset.add(element);
    }

    public boolean remove(@Nullable Object element)
    {
        return treeMultiset.remove(element);
    }

    public boolean _addAll(Collection<? extends E> elementsToAdd)
    {
        return treeMultiset.addAll(elementsToAdd);
    }

    public boolean removeAll(Collection<?> elementsToRemove)
    {
        return treeMultiset.removeAll(elementsToRemove);
    }

    public boolean retainAll(Collection<?> elementsToRetain)
    {
        return treeMultiset.retainAll(elementsToRetain);
    }

    public void clear()
    {
        treeMultiset.clear();
    }

    public Set<Multiset.Entry<E>> entrySet()
    {
        return treeMultiset.entrySet();
    }

    public Object[] toArray()
    {
        return treeMultiset.toArray();
    }

    public <T> T[] toArray(T[] a)
    {
        return treeMultiset.toArray(a);
    }

    public boolean containsAll(Collection<?> c)
    {
        return treeMultiset.containsAll(c);
    }

    public boolean removeIf(Predicate<? super E> filter)
    {
        return treeMultiset.removeIf(filter);
    }

    public Spliterator<E> spliterator()
    {
        return treeMultiset.spliterator();
    }

    public Stream<E> stream()
    {
        return treeMultiset.stream();
    }

    public Stream<E> parallelStream()
    {
        return treeMultiset.parallelStream();
    }

    public void forEach(Consumer<? super E> action)
    {
        treeMultiset.forEach(action);
    }
}