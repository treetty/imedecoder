package com.misingularity.util;

import com.misingularity.util.KeyBuilder;
import com.misingularity.util.RandomAccessor;

/**
 * Created by xiaoyun on 1/3/15.
 */
public class ForwardKeyBuilder implements KeyBuilder {
	
	StringBuilder sb = new StringBuilder();

    /**
     *
     * @param history
     * @param token
     * @param n
     * @return
     */
    public String build(RandomAccessor<String> history, String token, int n) {
        //StringBuilder sb = new StringBuilder();
    	sb.delete(0, sb.length());
        // Now we need to get rest history by padding <S> to end.
        int size = history.size();
        for (int i = 0; i < n - size - 1; ++i) {
            sb.append(KeyBuilder.start);
            sb.append(" ");
        }

        for (int i = size - n + 1; i < size; ++i) {
            if (i < 0) continue;
            sb.append(history.get(i));
            sb.append(" ");
        }
        sb.append(token);
        return sb.toString();
    }

    public String buildHistory(RandomAccessor<String> history, int n) {
        //StringBuilder sb = new StringBuilder();
    	sb.delete(0, sb.length());
        // Now we need to get rest history by padding <S> to end.
        int size = history.size();
        for (int i = 0; i < n - size - 1; ++i) {
            sb.append(KeyBuilder.start);
            sb.append(" ");
        }

        for (int i = size - n + 1; i < size; ++i) {
            if (i < 0) continue;
            sb.append(history.get(i));
            sb.append(" ");
        }
        if (sb.length() >0) sb.setLength(sb.length() - 1);
        return sb.toString();
    }
}
