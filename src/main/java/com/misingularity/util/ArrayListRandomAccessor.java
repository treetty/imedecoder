package com.misingularity.util;

import java.util.ArrayList;

/**
 * This is not really a safe implementation where the underly array can
 * Created by xiaoyun on 1/2/15.
 */
public class ArrayListRandomAccessor<T> implements RandomAccessor<T> {

    private final ArrayList<T> src;
    private int start;
    private int end;

    public ArrayListRandomAccessor(ArrayList<T> p) {
        src = p;
        start = 0;
        end = p.size();
    }

    public ArrayListRandomAccessor(ArrayList<T> p, int s, int e) {
        src = p;
        start = s;
        end = e;
    }

    /**
     * @return the number of item in the range.
     */
    public int size() {
        return end - start;
    }

    /**
     * @param idx
     * @return return the object at idx position.
     */
    public T get(int idx) {
        return src.get(idx + start);
    }
}
