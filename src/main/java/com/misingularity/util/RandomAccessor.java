package com.misingularity.util;

/**
 * RandomAccessor is useful for access item in an array or a range.
 * Created by xiaoyun on 1/2/15.
 */
public interface RandomAccessor<T> {

    /**
     * @return the number of item in the range.
     */
    public int size();

    /**
     * @param idx
     * @return return the object at idx position.
     */
    public T get(int idx);
}
