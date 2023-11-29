import java.lang.reflect.Array;
import java.util.*;

/**
 * MyHashSet is a custom implementation of the Set interface.
 *
 * <p>The implementation supports dynamic resizing of the underlying array when the load factor is exceeded,
 * ensuring optimal performance for a varying number of elements.
 *
 * @param <E> the type of elements maintained by this set
 * @see Set
 * @see HashSet
 * @see List
 */
public class MyHashSet<E> implements Set<E> {
    private List<E>[] backingStore;
    private static final int DEFAULT_INT_CAP = 16;
    private final double LOAD_FACTOR;
    private int size = 0, mod_count = 0;
    private boolean overFlowFlag = false;

    //Default Constructor
    public MyHashSet() {
        this(DEFAULT_INT_CAP, .75);
    }

    //Capacity Constructor
    public MyHashSet(int initialCapacity) {
        this(initialCapacity, .75);
    }

    //Specified Constructor
    public MyHashSet(int initialCapacity, double loadFactor) {
        if (loadFactor <= 0) {
            throw new IllegalArgumentException("load factor must be greater than 0");
        }

        if (initialCapacity < 0) {
            throw new IllegalArgumentException("capacity cannot be negative");
        }

        backingStore = new List[initialCapacity];
        LOAD_FACTOR = loadFactor;
    }


    /**
     * Returns the number of elements in this set (its cardinality).  If this
     * set contains more than {@code Integer.MAX_VALUE} elements, returns
     * {@code Integer.MAX_VALUE}.
     *
     * @return the number of elements in this set (its cardinality)
     */
    @Override
    public int size() {
        return overFlowFlag ? Integer.MAX_VALUE : size;
    }

    /**
     * Returns {@code true} if this set contains no elements.
     *
     * @return {@code true} if this set contains no elements
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns {@code true} if this set contains the specified element.
     * More formally, returns {@code true} if and only if this set
     * contains an element {@code e} such that
     * {@code Objects.equals(o, e)}.
     *
     * @param o element whose presence in this set is to be tested
     * @return {@code true} if this set contains the specified element
     * @throws ClassCastException   if the type of the specified element
     *                              is incompatible with this set
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified element is null and this
     *                              set does not permit null elements
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     */
    @Override
    public boolean contains(Object o) {
        classCompatibilityCheck(o.getClass());

        return backingStore[Math.abs(Objects.hashCode(o)) % backingStore.length].contains(o);
    }

    /**
     * Returns an iterator over the elements in this set.  The elements are
     * returned in no particular order (unless this set is an instance of some
     * class that provides a guarantee).
     *
     * @return an iterator over the elements in this set
     */
    @Override
    public Iterator<E> iterator() {
        return new MyIterator();
    }

    /**
     * This private inner class implements the Iterator interface to iterate over
     * elements in the Set.
     */
    private class MyIterator implements Iterator<E> {
        int outIndex = 0;
        int inIndex = 0;
        Object returnVal;
        int OgModCount = mod_count;

        /**
         * Returns true if there are more elements to iterate over.
         *
         * @return {@code true} if there are more elements, {@code false} otherwise.
         */
        public boolean hasNext() {
            return outIndex < backingStore.length || inIndex < backingStore[backingStore.length - 1].size();
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration.
         * @throws NoSuchElementException          if there are no more elements to iterate.
         * @throws ConcurrentModificationException if the underlying set is modified
         *                                         while iterating.
         */
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            //If the current position is not at the bottom of the current outer-index, continue on toward the bottom
            if (backingStore[outIndex].size() > inIndex) {
                returnVal = backingStore[outIndex].get(inIndex++);
            }
            //If the current position exceeds the length of the current outer-index, move to next outer index
            else {
                inIndex = 0;
                returnVal = backingStore[outIndex++].get(inIndex);
            }

            //check for concurrent mod
            if (OgModCount != mod_count)
                throw new ConcurrentModificationException("The Iterator has detected a modification to the Set. This is not allowed.");
            return (E) returnVal;
        }
    }

    /**
     * Returns an array containing all of the elements in this set.
     * If this set makes any guarantees as to what order its elements
     * are returned by its iterator, this method must return the
     * elements in the same order.
     *
     * <p>The returned array will be "safe" in that no references to it
     * are maintained by this set.  (In other words, this method must
     * allocate a new array even if this set is backed by an array).
     * The caller is thus free to modify the returned array.
     *
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return an array containing all the elements in this set
     */
    @Override
    public Object[] toArray() {
        Object[] outRay = new Object[size];
        int counter = 0;

        for (E el : this) {
            outRay[counter++] = el;
        }
        return outRay;
    }

    /**
     * Returns an array containing all of the elements in this set; the
     * runtime type of the returned array is that of the specified array.
     * If the set fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of the
     * specified array and the size of this set.
     *
     * <p>If this set fits in the specified array with room to spare
     * (i.e., the array has more elements than this set), the element in
     * the array immediately following the end of the set is set to
     * {@code null}.  (This is useful in determining the length of this
     * set <i>only</i> if the caller knows that this set does not contain
     * any null elements.)
     *
     * <p>If this set makes any guarantees as to what order its elements
     * are returned by its iterator, this method must return the elements
     * in the same order.
     *
     * <p>Like the {@link #toArray()} method, this method acts as bridge between
     * array-based and collection-based APIs.  Further, this method allows
     * precise control over the runtime type of the output array, and may,
     * under certain circumstances, be used to save allocation costs.
     *
     * <p>Suppose {@code x} is a set known to contain only strings.
     * The following code can be used to dump the set into a newly allocated
     * array of {@code String}:
     *
     * <pre>
     *     String[] y = x.toArray(new String[0]);</pre>
     * <p>
     * Note that {@code toArray(new Object[0])} is identical in function to
     * {@code toArray()}.
     *
     * @param a the array into which the elements of this set are to be
     *          stored, if it is big enough; otherwise, a new array of the same
     *          runtime type is allocated for this purpose.
     * @return an array containing all the elements in this set
     * @throws ArrayStoreException  if the runtime type of the specified array
     *                              is not a supertype of the runtime type of every element in this
     *                              set
     * @throws NullPointerException if the specified array is null
     */
    @Override
    public <T> T[] toArray(T[] a) {
        if (!this.getClass().getComponentType().isAssignableFrom(a.getClass())) {
            throw new ArrayStoreException("Passed array is of a type incompatible with the Set type");
        }

        if (a.length < size) {
            a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
        }

        int i = 0;

        for (E el : this) {
            a[i++] = (T) el;
        }
        //I debated whether to use a while loop to fill the rest of the passed collection, but decided this was most readable
        for (int j = i; j < a.length; j++) {
            a[i++] = null;
        }

        return a;
    }

    /**
     * Adds the specified element to this set if it is not already present
     * (optional operation).  More formally, adds the specified element
     * {@code e} to this set if the set contains no element {@code e2}
     * such that
     * {@code Objects.equals(e, e2)}.
     * If this set already contains the element, the call leaves the set
     * unchanged and returns {@code false}.  In combination with the
     * restriction on constructors, this ensures that sets never contain
     * duplicate elements.
     *
     * <p>The stipulation above does not imply that sets must accept all
     * elements; sets may refuse to add any particular element, including
     * {@code null}, and throw an exception, as described in the
     * specification for {@link Collection#add Collection.add}.
     * Individual set implementations should clearly document any
     * restrictions on the elements that they may contain.
     *
     * @param e element to be added to this set
     * @return {@code true} if this set did not already contain the specified
     * element
     * @throws UnsupportedOperationException if the {@code add} operation
     *                                       is not supported by this set
     * @throws ClassCastException            if the class of the specified element
     *                                       prevents it from being added to this set
     * @throws NullPointerException          if the specified element is null and this
     *                                       set does not permit null elements
     * @throws IllegalArgumentException      if some property of the specified element
     *                                       prevents it from being added to this set
     */
    @Override
    public boolean add(Object e) {
        boolean returnVal;
        classCompatibilityCheck(e.getClass());

        if (size > backingStore.length * LOAD_FACTOR)
            refactor();

        int index = Math.abs(Objects.hashCode(e)) % backingStore.length;

        if (backingStore[index] == null) {
            backingStore[index] = new ArrayList<>();
        }

        List<E> reference = backingStore[index];
        returnVal = reference.add((E) e);
        size++;
        mod_count++;

        if (size + 1 == Integer.MAX_VALUE && !overFlowFlag)
            overFlowFlag = true;

        return returnVal;
    }

    private void refactor() {
        Object[] holdingRay = toArray();
        backingStore = new List[size * 2];

        for (Object el : holdingRay) {
            add(el);
        }
    }

    /**
     * Removes the specified element from this set if it is present
     * (optional operation).  More formally, removes an element {@code e}
     * such that
     * {@code Objects.equals(o, e)}, if
     * this set contains such an element.  Returns {@code true} if this set
     * contained the element (or equivalently, if this set changed as a
     * result of the call).  (This set will not contain the element once the
     * call returns.)
     *
     * @param o object to be removed from this set, if present
     * @return {@code true} if this set contained the specified element
     * @throws ClassCastException            if the type of the specified element
     *                                       is incompatible with this set
     *                                       (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException          if the specified element is null and this
     *                                       set does not permit null elements
     *                                       (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws UnsupportedOperationException if the {@code remove} operation
     *                                       is not supported by this set
     */
    @Override
    public boolean remove(Object o) {
        classCompatibilityCheck(o.getClass());

        boolean returnVal = backingStore[Math.abs(o.hashCode()) % backingStore.length].remove(o);
        size--;
        mod_count++;

        return returnVal;
    }

    /**
     * Returns {@code true} if this set contains all of the elements of the
     * specified collection.  If the specified collection is also a set, this
     * method returns {@code true} if it is a <i>subset</i> of this set.
     *
     * @param c collection to be checked for containment in this set
     * @return {@code true} if this set contains all of the elements of the
     * specified collection
     * @throws ClassCastException   if the types of one or more elements
     *                              in the specified collection are incompatible with this
     *                              set
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified collection contains one
     *                              or more null elements and this set does not permit null
     *                              elements
     *                              (<a href="Collection.html#optional-restrictions">optional</a>),
     *                              or if the specified collection is null
     * @see #contains(Object)
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        boolean returnVal = true;

        for (Object el : c) {

            classCompatibilityCheck(el.getClass());
            returnVal = contains(el);
            if (!returnVal)
                break;
        }

        return returnVal;
    }

    /**
     * Adds all of the elements in the specified collection to this set if
     * they're not already present (optional operation).  If the specified
     * collection is also a set, the {@code addAll} operation effectively
     * modifies this set so that its value is the <i>union</i> of the two
     * sets.  The behavior of this operation is undefined if the specified
     * collection is modified while the operation is in progress.
     *
     * @param c collection containing elements to be added to this set
     * @return {@code true} if this set changed as a result of the call
     * @throws UnsupportedOperationException if the {@code addAll} operation
     *                                       is not supported by this set
     * @throws ClassCastException            if the class of an element of the
     *                                       specified collection prevents it from being added to this set
     * @throws NullPointerException          if the specified collection contains one
     *                                       or more null elements and this set does not permit null
     *                                       elements, or if the specified collection is null
     * @throws IllegalArgumentException      if some property of an element of the
     *                                       specified collection prevents it from being added to this set
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        for (Object el : c) {

            classCompatibilityCheck(el.getClass());
            add(el);
        }

        return true;
    }

    /**
     * Retains only the elements in this set that are contained in the
     * specified collection (optional operation).  In other words, removes
     * from this set all of its elements that are not contained in the
     * specified collection.  If the specified collection is also a set, this
     * operation effectively modifies this set so that its value is the
     * <i>intersection</i> of the two sets.
     *
     * @param c collection containing elements to be retained in this set
     * @return {@code true} if this set changed as a result of the call
     * @throws UnsupportedOperationException if the {@code retainAll} operation
     *                                       is not supported by this set
     * @throws ClassCastException            if the class of an element of this set
     *                                       is incompatible with the specified collection
     *                                       (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException          if this set contains a null element and the
     *                                       specified collection does not permit null elements
     *                                       (<a href="Collection.html#optional-restrictions">optional</a>),
     *                                       or if the specified collection is null
     * @see #remove(Object)
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        int oldSize = size;


        for (Object el : c) {

            classCompatibilityCheck(el.getClass());
            remove(el);
        }

        return oldSize == size;
    }

    /**
     * Removes from this set all of its elements that are contained in the
     * specified collection (optional operation).  If the specified
     * collection is also a set, this operation effectively modifies this
     * set so that its value is the <i>asymmetric set difference</i> of
     * the two sets.
     *
     * @param c collection containing elements to be removed from this set
     * @return {@code true} if this set changed as a result of the call
     * @throws UnsupportedOperationException if the {@code removeAll} operation
     *                                       is not supported by this set
     * @throws ClassCastException            if the class of an element of this set
     *                                       is incompatible with the specified collection
     *                                       (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException          if this set contains a null element and the
     *                                       specified collection does not permit null elements
     *                                       (<a href="Collection.html#optional-restrictions">optional</a>),
     *                                       or if the specified collection is null
     * @see #remove(Object)
     * @see #contains(Object)
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        classCompatibilityCheck(c.getClass());
        return false;
    }

    /**
     * Removes all of the elements from this set (optional operation).
     * The set will be empty after this call returns.
     *
     * @throws UnsupportedOperationException if the {@code clear} method
     *                                       is not supported by this set
     */
    @Override
    public void clear() {
        backingStore = new List[DEFAULT_INT_CAP];
        mod_count++;
    }

    private void classCompatibilityCheck(Class<?> o) {
        if (!this.getClass().getComponentType().isAssignableFrom(o)) {
            throw new ClassCastException("One or more of the elements passed as arguments is of a type incompatible with the Set type");
        }
    }
}