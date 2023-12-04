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
        return size() == 0;
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
        if (o != null) {
            classCompatibilityCheck(o);
        }

        //Index of interior list to be checked. Normally the local hash-code is called
        // but if the passed value is null, that needs to be hard-coded to 0, as null has no hash ability, and no other value can be assured to be in the outer index
        int indexToCheck = (o == null) ? 0 : Math.abs(Objects.hashCode(o)) % backingStore.length;

        return backingStore[indexToCheck] != null && backingStore[indexToCheck].contains(o);
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
        int inIndex = 1;
        boolean existsSubsequent = !isEmpty();
        Object returnVal;
        Object subsequent;
        int originalModCount = mod_count;

        private MyIterator() {
            if (existsSubsequent) {
                //If there is no list at the index in backingStore, iterate the pointer
                while (outIndex < backingStore.length && backingStore[outIndex] == null) {
                    outIndex++;
                }
                //The first position in every existing list always contains its first element
                subsequent = backingStore[outIndex].get(0);
            }
        }

        /**
         * Returns true if there are more elements to iterate over.
         *
         * @return {@code true} if there are more elements, {@code false} otherwise.
         */
        public boolean hasNext() {
            return existsSubsequent;
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
            //check for concurrent mod
            if (originalModCount != mod_count)
                throw new ConcurrentModificationException("The Iterator has detected a modification to the Set. This is not allowed.");

            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            //If the current cursor position is at the bottom of the current outer-index, set innerIndex to zero and iterate outerIndex
            if (backingStore[outIndex] == null || inIndex >= backingStore[outIndex].size()) {
                inIndex = 0;
                outIndex++;
            }

            //If there is no list at the index in backingStore, iterate the pointer
            while (outIndex < backingStore.length && backingStore[outIndex] == null) {
                outIndex++;
            }

            //If the previous search carried the cursor beyond the end of the Set, there are no more elements.
            if (outIndex >= backingStore.length)
                existsSubsequent = false;

            //existing subsequent is loaded into the returnVal, and then the position is filled with the results of the search if any
            returnVal = subsequent;
            if (existsSubsequent)
                subsequent = backingStore[outIndex].get(inIndex++);

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
        Object[] outRay = new Object[size()];
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
        //"Resize" passed array if to small to store all els of this Set by reassigning its reference to a correctly sized array of the same type
        if (a.length < size()) {
            a = (T[]) Array.newInstance(a.getClass().getComponentType(), size());
        }

        int i = 0;

        for (E el : this) {
            if (!a.getClass().getComponentType().isAssignableFrom(el.getClass())) {
                throw new ArrayStoreException("Incompatible array type for the elements in the list.");
            }
            a[i++] = (T) el;
        }
        //if any positions remain in the array, fill them with 'null's. the previous index counter is maintained to finish the job
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
        if (e != null)
            classCompatibilityCheck(e);

        boolean returnVal = false;

        //check is this Set has become unbalanced and balances it if it has
        if (size() > backingStore.length * LOAD_FACTOR)
            refactor();

        //The actual add logic only gets run if the Set doesn't already contain the passed el
        if (!contains(e)) {
            returnVal = addNotDuple(e);
        }

        return returnVal;
    }

    /**
     * Adds the specified element to the set if it is not already present,
     * ensuring that duplicate elements are not allowed. This method is
     * an internal helper method used by the {@code add} operation.
     *
     * <p>The element is added to the set only if no element {@code e2}
     * such that {@code Objects.equals(e, e2)} is already present. If the
     * set already contains the element, the call leaves the set unchanged
     * and returns {@code false}.
     *
     * <p>The method handles the initialization of interior lists, checks for
     * potential overflow, and updates the size and modification count of
     * the set accordingly.
     *
     * @param e the element to be added to the set
     * @return {@code true} if the set did not already contain the specified
     * element and the addition is successful; {@code false} otherwise
     */
    private boolean addNotDuple(Object e) {
        //Index of interior list to be amended. Normally the local hash-code is called, but if the passed value is null, that needs to be hard-coded to 0.
        int indexToAddTo = (e == null) ? 0 : Math.abs(Objects.hashCode(e)) % backingStore.length;

        //If there is no array at that index, initialize one.
        if (backingStore[indexToAddTo] == null)
            //This is the only way to initialize interior lists.
            backingStore[indexToAddTo] = new ArrayList<>();

        List<E> listToAmend = backingStore[indexToAddTo];

        //checks for an overflow, and then trips the flag. This cannot be undone except by refactor() of clear().
        if (!overFlowFlag && size + 1 == Integer.MAX_VALUE)
            overFlowFlag = true;

        //All that, to get to this. The actual add command.
        boolean returnVal = listToAmend.add((E) e);

        //meta data modification
        size++;
        mod_count++;

        return returnVal;
    }

    /**
     * Resizes the backing store of the set, doubling its capacity. This method
     * is invoked when the size of the set exceeds a certain threshold.
     * The resizing involves creating a new backing store with double the
     * capacity, copying elements from the current set to the new store,
     * and updating the set's internal state accordingly.
     * <p>
     * This method ensures that the set remains balanced and provides
     * sufficient capacity to accommodate additional elements without
     * frequent resizing.
     * </p>
     * <p>
     * Note: The implementation uses a three-step process involving
     * storing elements in an array, reassigning the backing store
     * reference, and copying elements to the new backing store.
     * </p>
     * <p>
     * This method is private and called internally by the {@code add} operation
     * when the size of the set exceeds a predefined threshold.
     * </p>
     */
    private void refactor() {
        //Best way I could think to make a stored copy of the data.
        Object[] holdingRay = toArray();

        //Reassigns the reference for the outer list in order to dump the old struct, and mark it for garbage collection.
        backingStore = new List[backingStore.length * 2];

        //copy out of storage to new struct
        //it is necessary that a refactor not double count length.
        size = 0;
        overFlowFlag = false;

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
        classCompatibilityCheck(o);

        //Index of interior list to remove from. Normally the local hash-code is called, but if the passed value is null, that needs to be hard-coded to 0.
        int indexToRemoveFrom = (o == null) ? 0 : Math.abs(Objects.hashCode(o)) % backingStore.length;

        //return false if there is no list at the given index, otherwise return is equal to the return of the local remove method
        boolean returnVal = backingStore[indexToRemoveFrom] != null && backingStore[indexToRemoveFrom].remove(o);

        //If something was removed, check if its list is now empty, and if so clear it. Dropping empty lists helps my iterator method.
        if (returnVal && backingStore[indexToRemoveFrom].isEmpty())
            backingStore[indexToRemoveFrom] = null;

        //if something was actually removed, update the meta-data
        if (returnVal) {
            size--;
            mod_count++;
        }

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

        //If a single value from the passed collection is not found in the Set, the return is set to false, and the loop stops looking.
        for (Object el : c) {
            classCompatibilityCheck(el);
            if (!contains(el)) {
                returnVal = false;
                break;
            }
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
        int oldMod = mod_count;

        for (Object el : c) {
            classCompatibilityCheck(el);

            if (!contains(el))
                add(el);
        }

        //Check for modification
        return mod_count != oldMod;
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
            classCompatibilityCheck(el);
            remove(el);
        }

        //Check for size change
        return oldSize != size;
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
        classCompatibilityCheck(c);
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
        size = 0;
        overFlowFlag = false;
        mod_count++;
    }

    private void classCompatibilityCheck(Object o) {
        // TODO: fix
//        for (E el : this) {
//            if (!o.getComponentType().isAssignableFrom(el.getClass())) {
//                throw new ClassCastException("Incompatible array type for the elements in the list.");
//            }
//            break;
//        }

//        if (!clazz.isInstance(o)) {
//            throw new ClassCastException("Input object is not compatible with the expected type");
//        }
    }
}