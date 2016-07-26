package com.misingularity.util;

/**
 * Created by xiaoyun on 7/16/14.
 */

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This build ssd based read only hashMap.
 *
 * For now, we assume that we can get everything in the memory, this is much easier to deal with.
 * We assume the key is always of String type.
 * <p>
 * We use two files one for fixed size, provide the entry point for the given slot, another for key/value
 * pair. In case their is collision, we simply chain them together.
 *
 */
public class SSDFixWidthSSTableBuilder {

    private float ratio = 0.75f;
    private boolean debug = true;

    public static class Entry implements Comparable<Entry> {
        public int index;
        public long key;
        public byte[] value;

        public Entry(long k, byte[] v) {
            key = k;
            value = v;
        }

        public void update(int modSize) {
            index = (int) key % modSize;
            if (index < 0) index += modSize;
        }

        public int compareTo(Entry compareEntry) {
            return index - compareEntry.index;
        }

        public String toString() {
            return "" + index + ":" + key;
        }
    }

    private ArrayList<Entry> entries = new ArrayList<Entry>();

    int valSize = 8;
    int maxTries = 10;

    public SSDFixWidthSSTableBuilder(int vsize) {
        valSize = vsize;
    }
    public SSDFixWidthSSTableBuilder(int vsize, int pMaxTries) {
        valSize = vsize;
        maxTries = pMaxTries;
    }

    public void put(String key, byte[] value) {
        long lkey = MurmurHash.hash64(key);
        entries.add(new Entry(lkey, value));
    }

    private int findFirstEmpty(long[] keys, long key, int maxTries) {
        int modSize = keys.length;
        int start = (int)key%modSize;
        if (start < 0) start += modSize;

        // We assume that zero is special value, there should be less than one in 266 billion chance that
        // we get the an real key has hash code as zero.
        for (int k = 0; k < maxTries; ++k) {
            int idx = (start + k) % modSize;
            if (keys[idx] == 0) return idx;
        }
        return -1;
    }

    // Now we write out to disk in the following format, first, we write out
    // first: the size of hash primary array, for pointers.
    // second: the index array which list the start position into randomaccessfile, and then size that one have to
    //         read to get all the entry.
    // third: we
    public void save(String path) throws IOException {
        // First figure out how many slots do we need to host this,
        int base = (int) (entries.size() / ratio);
        int modSize = getNextPrime(base);
        save(path, modSize);
    }


    // Now we write out to disk in the following format, first, we write out
    // first the meta data, the size of key, value and also max number of tries.
    // second the modsize of entries, with key and value.
    public void save(String path, int modSize) throws IOException {
        System.out.println("Using mode size " + modSize);
        int numOfZeros = 0;
        for (int i = 0; i < entries.size(); ++i) {
            entries.get(i).update(modSize);
            if (entries.get(i).key == 0) numOfZeros += 1;
        }
        System.out.println("Number of zero keys " + numOfZeros);
        Collections.sort(entries);

        for (int i = 0; i < entries.size(); ++i) {
            //if (i%1000 == 0 || debug) System.out.println(entries.get(i).toString());
        }

        long[] keys = new long[modSize];
        byte[] vals = new byte[modSize*valSize];

        // Deal with the corner case where the number of entries are really small.
        if (maxTries > modSize) maxTries = modSize;
        int count = 0;
        for (int i = 0; i < entries.size(); ++i) {
            long key = entries.get(i).key;
            int idx = findFirstEmpty(keys, key, maxTries);
            if (idx == -1) {
                count += 1;
            } else {
                keys[idx] = key;
                byte[] tval = entries.get(i).value;
                System.arraycopy(tval, 0, vals, idx*valSize, valSize);
            }
        }

        System.out.println("There are " + count + " entries skipped.");

        RandomAccessFile idxFile = new RandomAccessFile(path + SSDFixWidthSSTable.getPrefix(), "rw");
        idxFile.writeInt(8);
        idxFile.writeInt(valSize);
        idxFile.writeInt(maxTries);
        for (int idx = 0; idx < modSize; ++idx) {
            idxFile.writeLong(keys[idx]);
            idxFile.write(vals, idx*valSize, valSize);
        }
        // Close the files.
        idxFile.close();
    }


    private boolean isPrime(int s) {
        int uplimit = (int) Math.sqrt(s);
        for (int i = 2; i <= uplimit; ++i) {
            if (s % i == 0) return false;
        }
        return true;
    }

    private int getNextPrime(int s) {
        while (!isPrime(s)) {
            s += 1;
        }
        return s;
    }
}