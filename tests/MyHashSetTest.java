import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MyHashSetTest {

    Set<String> mySet;


    @BeforeEach
    void setUp() {
        mySet = new MyHashSet();
    }

    @Test
    void emptySetsize() {
        assertEquals(0, mySet.size(), "Expected size of empty set to be zero");
    }

    @Test
    void size_Normal() {
        prep();

        assertEquals(3, mySet.size(), "size() has failed to print true size");
    }

    @Test
    void size_Edge_resize() {
        prep();

        assertTrue(mySet.remove("Poe"));
        assertEquals(2, mySet.size(), "size() fails in to account for diminution");
    }

    @Test
    void isEmpty_Edge_InAndOut() {
        prep();

        mySet.clear();

        assertTrue(mySet.isEmpty(), "isEmpty() isn't returning true after a clear");
        assertEquals(0, mySet.size(), "Size is not decrementing to zero after clear");
    }

    @Test
    void contains_Normal() {
        prep();

        assertTrue(mySet.contains("Poe"), "contains() isn't finding added elements");
        assertFalse(mySet.contains("Midnights so dreary"), "contains() is returning true for elements never added to set");
    }

    @Test
    void contains_Edge_Refactor() {
        bigPrep();

        assertTrue(mySet.contains("30"));
    }

    @Test
    void contains_Edge_NullEl() {
        prep();
        assertFalse(mySet.contains(null), "null is being found as an element despite not being added");

        mySet.add(null);

        assertTrue(mySet.contains(null), "null is not being found as an element despite being added");
    }

    @Test
    void iterator_Normal() {
        prep();
        ArrayList<String> testable = new ArrayList<>();

        Iterator<String> sitter = mySet.iterator();

        while (sitter.hasNext()) {
            testable.add(sitter.next());
        }

        //check that the correct number of elements were copied out
        assertEquals(mySet.size(), testable.size(), "There is some issue with the number of elements the iterator is finding in the set");

        //check that each element in the Set appears in the copy out
        for (String word : mySet) {
            assertTrue(testable.contains(word), "The elements being returned by the iterator are not matching the elements in the Set");
        }
    }

    @Test
    void iterator_Edge_loadFactorHuge() {
        mySet = new MyHashSet<>(16, 100);
        bigPrep();

        ArrayList<String> testable = new ArrayList<>(mySet);

        //check that the correct number of elements were copied out
        assertEquals(mySet.size(), testable.size(), "There is some issue with the number of elements the iterator is finding in the Set when rebalance is prohibited");

        for (String word : mySet) {
            assertTrue(testable.contains(word), "The elements being returned by the iterator are not matching the elements in the Set when rebalance is prohibited");
        }
    }

    @Test
    void hasNext_Normal() {
        prep();
        Iterator<String> sitter = mySet.iterator();

        sitter.next();
        sitter.next();

        assertTrue(sitter.hasNext());
        sitter.next();

        assertThrows(NoSuchElementException.class, sitter::next);
    }

    @Test
    void hasNext_Edge_EmptySet() {
        Iterator<String> sitter = mySet.iterator();
        assertFalse(sitter.hasNext());
    }

    @Test
    void next_Normal() {
        prep();
        ArrayList<String> testable = new ArrayList<>();
        testable.add("Poe");
        testable.add("E.");
        testable.add("Near a raven");
        Iterator<String> sitter = mySet.iterator();

        for (int i = 0; i < 3; i++) {
            assertTrue(testable.contains(sitter.next()));
        }
    }

    @Test
    void next_Edge_concurrentMod() {
        Iterator<String> sitter = mySet.iterator();
        prep();

        assertThrows(ConcurrentModificationException.class, sitter::next);
    }

    @Test
    void toArray_Normal() {
        prep();
        ArrayList<String> testable = new ArrayList<>();
        testable.add("Poe");
        testable.add("E.");
        testable.add("Near a raven");

        Object[] outray = mySet.toArray();

        for (Object word : outray) {
            assertTrue(testable.contains(word), "Elements have been improperly add to the Set");
        }

        assertEquals(testable.size(), outray.length, "The wrong number of elements has been added to the set");
    }

    @Test
    void toArray_Edge_MultiTyping() {
        MyHashSet<Object> testingSet = new MyHashSet<Object>();

        testingSet.add("Hello");
    }

    //toArray add(null)(not duple null) remove containsAll addAll retainAll clear

    //assertThrows(ConcurrentModificationException.class, () -> {
    //            prep();
    //            while (sitter.hasNext()) {
    //                testable.add((String) sitter.next());
    //            }
    //        });

    void prep() {
        mySet.add("Poe");
        mySet.add("E.");
        mySet.add("Near a raven");
    }

    void bigPrep() {
        String valToAdd;

        for (int i = 0; i < 32; i++) {
            valToAdd = ((Integer) i).toString();
            mySet.add(valToAdd);
        }
    }
}