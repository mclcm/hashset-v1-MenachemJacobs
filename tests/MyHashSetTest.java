import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

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
    void size_Normal(){
        prep();

        assertEquals(3, mySet.size(), "size() has failed to print true size");
    }

    @Test
    void size_Edge_resize(){
        prep();

        assertTrue(mySet.remove("Poe"));
        assertEquals(2, mySet.size(), "size() fails in to account for diminution");
    }

    @Test
    void isEmpty_Edge_InAndOut(){
        prep();

        mySet.clear();

        assertTrue(mySet.isEmpty(), "isEmpty() isn't returning true after a clear");
        assertEquals(0, mySet.size(), "Size is not decrementing to zero after clear");
    }

    @Test
    void contains_Normal(){
        prep();

        assertTrue(mySet.contains("Poe"), "contains() isn't finding added elements");
        assertFalse(mySet.contains("Midnights so dreary"), "contains() is returning true for elements never added to set");
    }

    @Test
    void contains_Edge_Refactor(){
        String valToAdd;

        //knowing something about the internal structure of the array made me wonder if this would work
        for (int i = 0; i < 32; i++) {
            valToAdd = ((Integer)i).toString();
            mySet.add(valToAdd);
        }

        assertTrue(mySet.contains("30"));
    }

    @Test
    void iterator_Normal(){
        prep();
        ArrayList<String> testable = new ArrayList<>();
        int counter = 0;

        Iterator<String> sitter = mySet.iterator();

        for (String word : mySet) {
            testable.add(word);
        }
        while(sitter.hasNext()){
            testable.add(sitter.next());
        }

        for (String word : mySet) {
            assertTrue(testable.contains(word));
        }

        assertEquals(mySet.size(), testable.size());
    }

    // hasNext next toArray toArray add remove containsAll addAll retainAll clear

    void prep(){
        mySet.add("Poe");
        mySet.add("E.");
        mySet.add("Near a raven");
    }
}