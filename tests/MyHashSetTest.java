import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MyHashSetTest {

    Set mySet;


    @BeforeEach
    void setUp() {
         mySet = new MyHashSet();
    }

    @Test
    void emptySetsize() {
        assertEquals(0, mySet.size(), "Expected size of empty set to be zero");
    }
}