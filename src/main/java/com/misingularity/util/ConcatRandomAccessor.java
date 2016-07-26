package com.misingularity.util;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This utility allow us to treat things uniformly.
 *
 * Created by xiaoyun on 1/2/15.
 */
public class ConcatRandomAccessor<T> implements RandomAccessor<T> {

    public static class UnitRandomAccessor<T> implements RandomAccessor<T> {
        T unit;
        public UnitRandomAccessor(T u) { unit = u; }
        public int size() { return 1; }
        public T get(int idx) { return unit; }
    }

    private RandomAccessor<T> front;
    private RandomAccessor<T> back;
    
    private int front_size;
    private int back_size;

    // This allow us to create the RandomAccessor
    public ConcatRandomAccessor(RandomAccessor<T> first, RandomAccessor<T> second) {
        front = first;
        back = second;
        front_size = front.size();
        back_size = back.size();
    }

    public ConcatRandomAccessor(RandomAccessor<T> first, T second) {
        front = first;
        back = new UnitRandomAccessor<T>(second);
        front_size = front.size();
        back_size = back.size();
    }

    /**
     * @return the number of item in the range.
     */
    public int size() {
        //return front.size() + back.size();
        return front_size + back_size;
    }

    /**
     * @param idx
     * @return return the object at idx position.
     */
    public T get(int idx) {
        return idx < front_size ? front.get(idx) : back.get(idx - front_size);
    }
}
