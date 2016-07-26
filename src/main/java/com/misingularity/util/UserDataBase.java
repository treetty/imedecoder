package com.misingularity.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.misingularity.util.MurmurHash;
import com.misingularity.util.StringMap;

public class UserDataBase {
    private static HashMap<Long, Integer> cntChineseSeq;
    private static UserDataBase instance;
    private static LinkedList<Pair<Long, Long>> insertQueue;

    // private static HashMap<String, Integer> cntChineseSeqString;
    private static long numUnitGrams = 0;
    private static long numWords = 0;

    public static synchronized void initializeInstance() {
        if (cntChineseSeq == null) {
            cntChineseSeq = new HashMap<Long, Integer>();
            insertQueue = new LinkedList<Pair<Long, Long>>();
            // cntChineseSeqString = new HashMap<String, Integer>();
            instance = new UserDataBase();
        }
    }

    public static UserDataBase getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                    UserDataBase.class.getSimpleName()
                            + " is not initialized, call initializeInstance(..) method first.");
        }

        return instance;
    }

    private static boolean isUnitGram(String st) {
        // Here we assume that all of the string should be like
        // each chinese character are delimit by one space.
        return st.length() <= 2;
    }

    public static synchronized void removeAllElementsBeforeTimeMark(long timeMark) {
        while (!insertQueue.isEmpty()) {
            Pair<Long, Long> p = insertQueue.element();
            if (p.first <= timeMark) {
                if (cntChineseSeq.containsKey(p.second)) {
                    int cnt = cntChineseSeq.get(p.second) - 1;
                    if (cnt > 0) {
                        cntChineseSeq.put(p.second, cnt);
                    } else {
                        cntChineseSeq.remove(p.second);
                    }
                }
                insertQueue.poll();
                --numWords;
            } else {
                break;
            }
        }
    }

    public static synchronized void addChineseSeq(String st, long timeMark) {
        if (isUnitGram(st)) {
            ++numUnitGrams;
        }
        ++numWords;

        long hashKey = MurmurHash.hash64(st);
        if (!cntChineseSeq.containsKey(hashKey)) {
            cntChineseSeq.put(hashKey, 1);
        } else {
            int cnt = cntChineseSeq.get(hashKey) + 1;
            cntChineseSeq.put(hashKey, cnt);
        }
        insertQueue.addLast(new Pair<Long, Long>(timeMark, hashKey));
    }

    public static int getCount(long hashKey) {
        if (!cntChineseSeq.containsKey(hashKey)) {
            return 0;
        } else {
            return cntChineseSeq.get(hashKey);
        }
    }

    public static int getCount(String st) {
        return getCount(MurmurHash.hash64(st));
    }

    public static long getNumWords() {
        return numWords;
    }

    public static long getNumUnitGrams() {
        return numUnitGrams;
    }

    public static void CheckOut() {
        System.out.println("SIZE: " + cntChineseSeq.size());
    }
}