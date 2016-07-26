package com.misingularity.util;

/**
 * Created by xiaoyun on 7/16/14.
 */

import java.io.*;
import java.util.ArrayList;
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
public class SSDSSTableBuilder {

    private float ratio = 0.75f;

    private ArrayList<String> keys = new ArrayList<String>();
    private ArrayList<String> values = new ArrayList<String>();


    public void put(String key, String value) {
        keys.add(key);
        values.add(value);
    }

    // Now we write out to disk in the following format, first, we write out
    // first: the size of hash primary array, for pointers.
    // second: the index array which list the start position into randomaccessfile, and then size that one have to
    //         read to get all the entry.
    // third: we
    public void save(String path) throws IOException {
        // First figure out how many slots do we need to host this,
        int base = (int) (keys.size() / ratio);
        int modSize = getNextPrime(base);

        // Build the inverted list
        ArrayList<LinkedList<Integer>> inverted = new ArrayList<LinkedList<Integer>>(modSize);
        for (int idx = 0; idx < modSize; ++idx) {
            inverted.add(new LinkedList<Integer>());
        }

        for (int i = 0; i < keys.size(); ++i) {
            int idx = keys.get(i).hashCode() % modSize;
            if (idx < 0) idx += modSize;
            inverted.get(idx).add(new Integer(i));
        }

        RandomAccessFile idxFile = new RandomAccessFile(path + SSDSSTable.getIdxPrefix(), "rw");
        RandomAccessFile datFile = new RandomAccessFile(path + SSDSSTable.getDatPrefix(), "rw");

        for (int idx = 0; idx < modSize; ++idx) {
            List<Integer> idxs = inverted.get(idx);

            if (idxs.size() == 0) {
                idxFile.writeInt(-1);
            } else {

                // Really means termination.
                idxFile.writeInt((int)datFile.length());

                // The strategy is we write current entry out, and have the next entry in the same slot carry
                // size, and file position of this entry, and have index file contain the size and file position for
                // last entry in the chain, that way, we can always figure out the right thing.
                for (Integer i : inverted.get(idx)) {
                    // Now write it out.
                    byte[] ba_key = keys.get(i).getBytes();
                    byte[] ba_val = values.get(i).getBytes();

                    datFile.writeShort(ba_key.length);
                    datFile.write(ba_key);

                    datFile.writeInt(ba_val.length);
                    datFile.write(ba_val);
                }
                datFile.writeShort(-1);
            }
        }

        // Close the files.
        datFile.close();
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
