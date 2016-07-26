package com.misingularity.util;

/**
 * This is a simple map that always use string as key. But can have
 * any type as value.
 *
 * Created by xiaoyun on 1/15/15.
 */
public interface StringMap<V> {
    public V get(String key);

    public V get(long hashKey);
}
