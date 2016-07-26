package com.misingularity.util;

import java.util.Arrays;
import java.io.*;

/**
 * Basic BloomFilter class. There are numBits bits in the bloomfilter and 4 
 * hash functions. 
 * If there are n elements which would be inserted into bloomfilter. The optimal
 * numBits is k*n/ln(2) where k is the number of hash functions. In this case
 * the false postive rate is (1/2)^k.
 */
public class BloomFilter {

    private int numBits;
    private byte[] byteArray; 
    private final int byteSize = Byte.SIZE;

    private int normalHash(String key, int mod) {
        int tmp = 0;
        for (int i = 0; i < key.length(); i++) {
            tmp = (tmp * 31 + (int)(key.charAt(i))) % mod;
        }
        return tmp;
    }

    // Align numBits by Byte.SIZE. 
    public BloomFilter(int m) {
        numBits = (m / byteSize + 1) * byteSize;
        byteArray = new byte[numBits / byteSize];
        Arrays.fill(byteArray, (byte) 0);
    }

    public BloomFilter(String path) throws IOException {
        load(path);
    }

    public int getBit(int pos) {
        return (byteArray[pos / byteSize] >> (pos % byteSize)) & 1;
    }

    public void addBit(int pos) {
        byteArray[pos / byteSize] |= 1 << (pos % byteSize); 
    }

    public int queryElement(String key) { 
        return queryElement(MurmurHash.hash64(key), MurmurHash.hash32(key));
    }

    public void addElement(String key) {
        addElement(MurmurHash.hash64(key), MurmurHash.hash32(key));
    }

    public long normalLongHash(long h) {
        h ^= (h >>> 40) ^ (h >>> 24);
        return h ^ (h >>> 17) ^ (h >>> 7);
    }

    public int normalIntHash(int h) {
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }

    public int queryElement(long hash64Key, int hash32Key) {
        if (getBit((int)(hash64Key % numBits + numBits) % numBits) == 0) return 0;
        if (getBit((int)(normalLongHash(hash64Key) % numBits + numBits) % numBits) == 0) return 0;
        if (getBit((int)(hash32Key % numBits + numBits) % numBits) == 0) return 0;
        if (getBit((int)(normalIntHash(hash32Key) % numBits + numBits) % numBits) == 0) return 0;
        return 1;
    }

    public void addElement(long hash64Key, int hash32Key) {
        addBit((int)(hash64Key % numBits + numBits) % numBits);
        addBit((int)(normalLongHash(hash64Key) % numBits + numBits) % numBits);
        addBit((int)(hash32Key % numBits + numBits) % numBits);
        addBit((int)(normalIntHash(hash32Key) % numBits + numBits) % numBits);
    }

    public void save(String path) throws IOException {
        RandomAccessFile idxFile = new RandomAccessFile(path + ".bf", "rw");
        idxFile.writeInt(numBits);
        idxFile.write(byteArray);
        idxFile.close();
    }

    public void load(String path) throws IOException {
        RandomAccessFile idxFile = new RandomAccessFile(path + ".bf", "r");
        numBits = idxFile.readInt();
        byteArray = new byte[numBits / byteSize];
        idxFile.read(byteArray);
        idxFile.close();
    }
}