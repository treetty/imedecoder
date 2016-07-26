package com.misingularity.util;


import java.io.*;

/**
 * This class implements the index portion of SSDByteTrie. We assume there are multiple data items for the
 * same leaf node. We further assume that there are serializer can serialize and deserialize array of data item.
 * Note that data item can be pointers to other tables.
 *
 * RandomAccessFile.
 *
 * <p>
 * Created by xiaoyun on 7/24/14.
 */
public class SSDByteTrieIndex<D> {

    public static boolean isGoodKey(byte[] key) {
        for (int i = 0; i < key.length; ++i) {
            if (key[i] == zero) return false;
        }
        return true;
    }

    public static final byte zero = (byte) 0;


    // This is a bit scary, the format for the indexing part is the follows:
    // short: the number of branch. 2 bytes
    // float: the gain that can be used for optimization. 4 bytes.
    // byte array with number of branches. size bytes
    // offset array in int.
    public static int getOffset(int numOfBranches, int idx) {
        return 6 + numOfBranches + 4*idx;
    }


    /**
     * By not claim it to be static, we make Cursor aware of RandomAccessFile for index.
     *
     * The following format is assumed for the trieEntry:
     * The byte itself lives in the parent block:
     * for each trieEntry:
     * the number of children (int)
     * gain in float
     * sorted arrangement of bytes that are used as key.
     * corresponding index (pointers to matches) in index file.
     *
     * byte zero is termination, the pointer of byte zero contain the pointer/size of payload
     * in the data file.
     */
    public class Cursor {
        // These are scratch space, this is not thread safe.
        long pos = 0L;
        private int size = -1;
        private byte[] symbols = new byte[256];
        @SuppressWarnings("unused")
		private float gain = 0;
        private int idx = -1;
        private int leaf_idx = -1;
        public int depth = -1;
        public int start = -1;

        public Cursor(int s) {
            start = s;
            prepare();
        }

        public Cursor(Cursor c) {
            pos = c.pos;
            size = c.size;
            symbols = c.symbols.clone();
            gain = c.gain;
            idx = c.idx;
            leaf_idx = c.leaf_idx;
            depth = c.depth;
            start = c.start;
        }


        // We assume the input is the sorted.
        public int binarySearch(byte[] vec, int stt, int end, byte key) {
            if (end <= stt) {
                throw new RuntimeException("Not a valid range -> " + stt + ":" + end);
            } else if (end == stt + 1) {
                return vec[stt] == key ? stt : key < vec[stt] ? -stt - 1 : -end - 1;
            } else {
                int mid = (end + stt) / 2;
                if (vec[mid] == key) return mid;
                return key < vec[mid] ? binarySearch(vec, stt, mid, key) : binarySearch(vec, mid, end, key);
            }
        }

        private void prepare() {
            try {
                idxFile.seek(pos);
                size = idxFile.readShort();
                gain = idxFile.readFloat();
                int lsize = idxFile.read(symbols, 0, size);
                if (lsize != size) {
                    throw new RuntimeException("Error: expecting " + size + " but got " + lsize + " bytes.");
                }
                leaf_idx = binarySearch(symbols, 0, size, zero);
                depth += 1;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // This return true if ch is valid next state, and it will move to next stage. Otherwise, it return false.
        public boolean moveToNext(byte ch) throws IOException {
            idx = binarySearch(symbols, 0, size, ch);
            if (idx >= 0) {
                idxFile.seek(pos + getOffset(size, idx));
                pos = idxFile.readInt();
                prepare();
                return true;
            } else {
                return false;
            }
        }

        public boolean hasLeaf() {
            return leaf_idx >= 0;
        }

        public SSDByteTrieItems<D> readItems() {
            SSDByteTrieItems<D> ress = serializer.loadFromIndexFile(idxFile, pos, size, leaf_idx);
            ress.depth = depth;
            return ress;
        }

        // This is used clone a cursor so that we can check different path.
        public Cursor clone() {
            return new Cursor(this);
        }
    }


    //private final static Logger LOGGER = Logger.getLogger(SSDByteTrie.class.getName());
    private RandomAccessFile idxFile;
    private SSDByteTrieItemsSerializer<D> serializer;

    public SSDByteTrieIndex(String path, SSDByteTrieItemsSerializer<D> s) {
        serializer = s;
        try {
            idxFile = new RandomAccessFile(path, "r");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Cursor newCursor(int start) {
        return new Cursor(start);
    }

    // Make sure we release the resource even we forget to close files.
    public void close() {
        try {
            idxFile.close();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
