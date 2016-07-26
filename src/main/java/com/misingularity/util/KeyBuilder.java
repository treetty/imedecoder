package com.misingularity.util;

import com.misingularity.util.RandomAccessor;

/**
 * This interface is used to build the key representation ngram for given history, token and ngram order. We
 * can have two different implementation: forward or backward implementation.
 *
 * Created by xiaoyun on 1/3/15.
 */
public interface KeyBuilder {

    public static final String start = "<s>";
    public static final String end = "</s>";
    public static final String delimiter = " ";

    public static final byte[] startBytes = start.getBytes();
    public static final byte[] endBytes = end.getBytes();
    public static final byte[] delimiterBytes = delimiter.getBytes();

    public static final int lenStart = startBytes.length;
    public static final int lenEnd = endBytes.length;
    public static final int lenDelimiter = delimiterBytes.length;
    public static final int lenNormal = 3;

    /**
     *
     * @param history
     * @param token
     * @param order
     * @return representation of the ngram given history and current token at the order.
     */
    public String build(RandomAccessor<String> history, String token, int order);


    public String buildHistory(RandomAccessor<String> history, int order);
}
