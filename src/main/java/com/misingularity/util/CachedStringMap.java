package com.misingularity.util;

import java.util.HashSet;
import java.util.Map;
import java.util.ArrayList;

/**
 * Created by xiaoyun on 1/15/15.
 */
public class CachedStringMap<V> implements StringMap<V> {
    ArrayList<V> cacheValue;
    long[] cacheKey;
    StringMap<V> src;
    int cacheSize;

    public CachedStringMap(StringMap<V> s, int maxs) {
        cacheSize = maxs;
        src = s;
        cacheValue = new ArrayList<V>();
        cacheKey = new long[cacheSize];
        for (int i = 0; i < cacheSize; i++) {
            cacheValue.add(null);
        }
    }

    public V get(long hashKey) {
        int idx = ((int)(hashKey % cacheSize) + cacheSize) % cacheSize;

        V value = cacheValue.get(idx);
        if (value != null && cacheKey[idx] == hashKey) {
            return value;
        } else {
            cacheKey[idx] = hashKey;
            value = src.get(hashKey);
            cacheValue.set(idx, value);
            return value;
        }
    }

    public V get(String key) {
        return get(MurmurHash.hash64(key));
    }
}
