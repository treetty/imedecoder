package com.misingularity.util;

import com.misingularity.util.KeyBuilder;
import com.misingularity.util.RandomAccessor;

/**
 * Created by xiaoyun on 1/3/15.
 */
public class BackwardKeyBuilder implements KeyBuilder {

    public String build(RandomAccessor<String> history, String token, int n) {
        StringBuilder sb = new StringBuilder();
        sb.append(token);
        for (int i = history.size() - 1; i > history.size() - n && i >= 0; i--) {
            sb.append(" ");
            sb.append(history.get(i));
        }

        // Now we need to get rest history by padding <S> to end.
        for (int i = 0; i < n - history.size() - 1; ++i) {
            sb.append(" ");
            sb.append(KeyBuilder.start);
        }
        return sb.toString();
    }

    public String buildHistory(RandomAccessor<String> history, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = history.size() - 1; i > history.size() - n && i >= 0; i--) {
            sb.append(history.get(i));
            sb.append(" ");
        }

        // Now we need to get rest history by padding <S> to end.
        for (int i = 0; i < n - history.size() - 1; ++i) {
            sb.append(KeyBuilder.start);
            sb.append(" ");
        }
        if (sb.length() >0) sb.setLength(sb.length() - 1);
        return sb.toString();
    }
}
