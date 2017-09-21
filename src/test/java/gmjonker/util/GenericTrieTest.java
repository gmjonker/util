package gmjonker.util;

import com.google.common.base.Splitter;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static gmjonker.util.CollectionsUtil.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.IsEqual.equalTo;

public class GenericTrieTest
{
    @Test
    public void add() throws Exception
    {
        GenericTrie<String, String> trie = new GenericTrie<>();
        Splitter splitter = Splitter.on(' ');
        trie.add(splitter.split("eat").iterator(), "eat");
//        trie.add(splitter.split("eat a").iterator(), "eata");
        trie.add(splitter.split("eat a frog").iterator(), "eatafrog");
        trie.add(splitter.split("eat a dog").iterator(), "eatadog");
        trie.add(splitter.split("hello world").iterator(), "helloworld");
        trie.add(splitter.split("y").iterator(), "y");
        trie.add(splitter.split("y n n n y").iterator(), "ynnny");

//        Node.printSubTrie(trie, 0);
        trie.print();

        System.out.println();

        assertThat(trie.get(splitter.split("eat a frog").iterator()), equalTo("eatafrog"));
        assertThat(trie.get(splitter.split("trump").iterator()), equalTo(null));
        assertThat(trie.get(splitter.split("eat").iterator()), equalTo("eat"));
        assertThat(trie.get(splitter.split("eat a").iterator()), equalTo(null));
        assertThat(trie.get(splitter.split("eat a frog daily").iterator()), equalTo(null));
        assertThat(trie.get(splitter.split("eat a a a").iterator()), equalTo(null));
        assertThat(trie.get(splitter.split("hello world").iterator()), equalTo("helloworld"));
        assertThat(trie.get(splitter.split("hello").iterator()), equalTo(null));
        assertThat(trie.get(splitter.split("").iterator()), equalTo(null));
        
        System.out.println();
        
        String text = "sometimes you want to eat a frog or eat a cat or say hello or hello world y n n n";
        System.out.println("text = " + text);
        Iterable<String> split = splitter.split(text);
        List<String> words = toList(split);
        Set<Pair<String, Integer>> results = new HashSet<>();
        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);
            Pair<String, Integer> result = trie.getLongestFrom(words, i);
            System.out.println(word + " -> " + result);
            results.add(result);
        }
        assertThat(results, containsInAnyOrder(Pair.of("eat", 1), Pair.of("eatafrog", 3), Pair.of("helloworld", 2), Pair.of("y", 1), null));
    }

    @Test
    public void get() throws Exception
    {
    }

}