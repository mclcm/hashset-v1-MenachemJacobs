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
        assertEquals(2, mySet.size(), "size() fails to account for diminution");
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

        String errorOne = "The elements being returned by the iterator are not matching the elements in the Set";
        String errorTwo = "Not all elements in the Set are being returned by the iterator";

        //check that each element in the Set appears in the copy out and that the elements in the copy out all appear in the Set
        eachContainsEach(mySet, testable.toArray(), errorOne, errorTwo);
    }

    @Test
    void iterator_Edge_loadFactorHuge() {
        mySet = new MyHashSet<>(16, 100);
        bigPrep();

        ArrayList<String> testable = new ArrayList<>(mySet);

        //check that the correct number of elements were copied out
        assertEquals(mySet.size(), testable.size(), "There is some issue with the number of elements the iterator is finding in the Set when rebalance is prohibited");

        String errorOne = "The elements being returned by the iterator are not matching the elements in the Set when rebalance is prohibited";
        String errorTwo = "Not all elements in the Set are being returned by the iterator when rebalance is prohibited";

        eachContainsEach(mySet, testable.toArray(), errorOne, errorTwo);
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
            assertTrue(testable.contains(word), "Elements have been improperly added to the Set");
        }

        assertEquals(testable.size(), outray.length, "The wrong number of elements have been added to the set");
    }

    @Test
    void toArray_Edge_MultiTyping() {
        MyHashSet<Object> multiTypedSet = new MyHashSet<>();

        multiTypedSet.add("Hello");
        multiTypedSet.add(7);
        multiTypedSet.add(true);

        Object[] testable = multiTypedSet.toArray();
        int counter = 0;

        for (Object el : testable) {
            assertTrue(multiTypedSet.contains(el), "Multityping the Set is messing with the elements returned by the toArray() method");
        }

        for (Object el : multiTypedSet) {
            assertEquals(testable[counter++], el, "Multityping the Set is breaking the order of return for the toArray() method");
        }
    }

    @Test
    void toArray_typedReturn_Normal() {
        bigPrep();

        String[] testable = mySet.toArray(new String[40]);
        int counter = 0;

        for (String word : testable) {
            if (counter++ < mySet.size())
                assertTrue(mySet.contains(word), "Elements in toArray(T[] a) are not found in the Set");
            else
                assertNull(word, "toArray(T[] a) is not end filling with null values");
        }

        counter = 0;

        for (String word : mySet) {
            while (!testable[counter].equals(word) && counter < mySet.size()) {
                counter++;
            }
            assertTrue(counter < mySet.size(), "Elements from mySet are nowhere to be found in the return from toArray(T[] a)");
        }
    }

    @Test
    void toArray_typedReturn_Edge_passedArrayToShort() {
        bigPrep();
        String[] testable = mySet.toArray(new String[25]);

        assertEquals(mySet.size(), testable.length, "toArray(T[] a) isn't resizing past arrays properly");

        int counter = 0;

        String errorOne = "Element not present in the Set are being returned by the toArray(T[] a) if the passed array has been resized";
        String errorTwo = "Elements from the Set aren't being found in the return from toArray(T[] a) if if the passed array has been resized";

        eachContainsEach(mySet, testable, errorOne, errorTwo);
    }

    @Test
    void toArray_typedReturn_Edge_incompatiblePassedArrayType() {
        prep();
        assertThrows(ArrayStoreException.class, () -> {
            mySet.toArray(new Boolean[3]);
        }, "toArray(T[] a) isn't throwing an error when passed an array of incompatible type");
    }

    @Test
    void add_Normal() {
        prep();
        String wordToAdd = "Midnights so dreary, tired and weary";

        assertFalse(mySet.contains(wordToAdd));
        assertTrue(mySet.add(wordToAdd), "Element not already in the Set is being rejected by the add() method");
        assertTrue(mySet.contains(wordToAdd), "Element added to the Set is not being found within it");
    }

    @Test
    void add_Edge_addNull() {
        prep();

        assertFalse(mySet.contains(null));
        assertTrue(mySet.add(null), "null value is being rejected by the add()");
        assertTrue(mySet.contains(null), "null value added to the Set is not being found within it");
    }

    @Test
    void add_Edge_Duplication() {
        prep();

        assertFalse(mySet.contains(null));
        assertTrue(mySet.add(null), "null value is being rejected by the add()");

        int oldSize = mySet.size();

        assertFalse(mySet.add(null), "null value is not being rejected by the added despite being a duplicate");

        assertEquals(oldSize, mySet.size());
    }

    @Test
    void remove_Normal() {
        prep();
        int counter = mySet.size();

        for (Object word : mySet.toArray()) {
            assertTrue(mySet.remove(word), "An element in the Set cannot be found in order to remove() it");
            assertEquals(--counter, mySet.size(), "Size is not decreasing as expected after a remove()");
        }

        assertTrue(mySet.isEmpty(), "Set is not empty despite remove()ing everything");
    }

    @Test
    void remove_Edge_removeNonExistent() {
        prep();
        int oldSize = mySet.size();

        assertFalse(mySet.remove("Midnights so dreary, tired and weary"));
        assertEquals(oldSize, mySet.size());
    }

    @Test
    void containsAll_Normal() {
    }

    //addAll retainAll clear

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

    void eachContainsEach(Set<String> m, Object[] n, String setDoesNotContain, String arrayDoesNotContain) {
        int counter = 0;

        for (Object word : n) {
            assertTrue(m.contains(word), setDoesNotContain);
        }

        for (String word : m) {
            while (!n[counter].equals(word) && counter < m.size()) {
                counter++;
            }
            assertTrue(counter < m.size(), arrayDoesNotContain);
        }
    }
}