package gmjonker.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Trie where the keys are of type Iterable<T>, instead of the more common String. It can for instance be used to map sequences of words.     
 */
// PERFORMANCE: use map only when there are multiple values, otherwise simple field?
public class GenericTrie<T,V> extends Node<T,V>
{
    public void add(Iterable<T> iterable, V value)
    {
        add(iterable.iterator(), value);
    }

    /**
     * Matches the longest possible sublist from tokens, starting at index.
     * @return Pair of found value and length of matched key, or null if nothing was found
     **/
    public Pair<V, Integer> getLongestFrom(List<T> tokens, int index)
    {
        return getLongest(tokens, index, 0, null);
    }


    public void print()
    {
        System.out.println("Node: ROOT -> " + value);
        printSubTrie(this, 4);
    }
}

class Node<T,V>
{
    V value;
    
    private Map<T,Node<T,V>> children = new HashMap<>();
    
    private static final LambdaLogger log = new LambdaLogger(Node.class);

    public void add(Iterator<T> tokenIterator, V value)
    {
        if (tokenIterator.hasNext()) 
        {
            T nextToken = tokenIterator.next();
            Node<T,V> nextNode = children.get(nextToken);
            if (nextNode == null) {
                nextNode = new Node<>();
                children.put(nextToken, nextNode);
            }
            nextNode.add(tokenIterator, value);
        } else {
            this.value = value;
        }
    }
    
    /** Matches the complete token iterator. **/
    public V get(Iterator<T> tokenIterator)
    {
        if (tokenIterator.hasNext())
        {
            T nextToken = tokenIterator.next();
            Node<T,V> nextNode = children.get(nextToken);
            if (nextNode == null) {
                log.trace("Next token not found: '{}'", nextToken);
                return null;
            } else {
                return nextNode.get(tokenIterator);
            }
        }
        return value;
    }
    
    /** 
     * Matches the longest possible sublist from tokens, starting at index.
     * @return Pair of found value and length of matched key, or null if nothing was found
     **/
    Pair<V,Integer> getLongest(List<T> tokens, int index, int currentDepth, Pair<V,Integer> lastFoundResult)
    {
        log.trace("getLongestFrom: value={}, index={}", value, index);
        if (value != null) {
            lastFoundResult = Pair.of(value, currentDepth);
        }
        if (index < tokens.size())
        {
            T nextToken = tokens.get(index);
            log.trace("getLongestFrom: nextToken = {}", nextToken);
            Node<T,V> nextNode = children.get(nextToken);
            log.trace("getLongestFrom: nextNode = {}", nextNode);
            if (nextNode != null) {
                return nextNode.getLongest(tokens, index + 1, currentDepth + 1, lastFoundResult);
            }
        }
        return lastFoundResult;
    }

//    /** Matches the longest possible sublist from tokens, starting at index. **/
//    V getLongest(List<T> tokens, int index, V lastFoundValue)
//    {
//        log.trace("getLongest: value={}, index={}", value, index);
//        if (value != null) {
//            lastFoundValue = value;
//        }
//        if (index < tokens.size())
//        {
//            T nextToken = tokens.get(index);
//            log.trace("getLongest: nextToken = {}", nextToken);
//            Node<T,V> nextNode = children.get(nextToken);
//            log.trace("getLongest: nextNode = {}", nextNode);
//            if (nextNode != null) {
//                return nextNode.getLongest(tokens, index + 1, lastFoundValue);
//            }
//        }
//        return lastFoundValue;
//    }
//
    @Override
    public String toString()
    {
        return "Node{" +
                "value=" + value +
                ", children=" + children.keySet() +
                '}';
    }
    
    void printSubTrie(Node<T, V> node, int indent)
    {
        for (T token : node.children.keySet()) {
            Node<T, V> childNode = node.children.get(token);
            System.out.println(StringUtils.repeat(" ", indent) + "Node: " + token + " -> " + childNode.value);
            printSubTrie(childNode, indent + 4);
        }        
    }
}