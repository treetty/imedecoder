package com.misingularity.util;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * We need to be able to find out the play load directly in order to save the disk access.
 *
 * Created by xiaoyun on 7/16/14.
 */
public class SSDFixWidthSSTable implements StringMap<byte[]> {

    public static String getPrefix() { return ".fwt"; }

    public static SSDFixWidthSSTable load(String path) throws IOException {
        RandomAccessFile idxFile = new RandomAccessFile(path + SSDFixWidthSSTable.getPrefix(), "r");
        return new SSDFixWidthSSTable(idxFile);
    }

    // This give the size of key and value in bytes.
    private static int metaSize = 12;
    private int keySize = 4;
    private int valSize = 4;
    private int maxTries = 1;
    private int entrySize;
    
    private byte[] res;
    private ByteBuffer buffer;

    private RandomAccessFile idxFile;
    private int modSize;

    public int getValueSize() { return valSize; }

    public SSDFixWidthSSTable(RandomAccessFile i) throws IOException {
        idxFile = i;
        idxFile.seek(0);
        keySize = idxFile.readInt();
        valSize = idxFile.readInt();
        maxTries = idxFile.readInt();
        entrySize = keySize + valSize;
        modSize = ((int)idxFile.length()- metaSize)/entrySize;
        res = new byte[valSize];
        buffer = ByteBuffer.allocate(maxTries * entrySize);

        System.out.println("keySize = " + keySize);
        System.out.println("valSize = " + valSize);
        System.out.println("maxTries = " + maxTries);
        System.out.println("modSize = " + modSize);

    }

    private long getPosition(int idx) {
        return metaSize + idx*entrySize;
    }

    /**
     * This method test whether input key is in the hashtable, and if it is, copy
     * the value into supplied byte array and return true, otherwise return false,
     * and the value is garbage.
     *
     * Note that we only check up to numOfCopies entry, beyond that, we just give up.
     * For operating on ssd, this seems to be a reasonable approach.
     *
     * @param key
     * @param value
     * @return
     * @throws IOException
     */
    public boolean getValue(String key, byte[] value) {
        return getValue(MurmurHash.hash64(key), value);
    }

    @Override
    public byte[] get(String key) {
        if (getValue(key, res)) return res;
        return null;
    }

    /**
     * This method test whether input key is in the hashtable, and if it is, copy
     * the value into supplied byte array and return true, otherwise return false,
     * and the value is garbage.
     *
     * Note that we only check up to numOfCopies entry, beyond that, we just give up.
     * For operating on ssd, this seems to be a reasonable approach.
     *
     * @param hashCode
     * @param value
     * @return
     * @throws IOException
     */
    public boolean getValue(long hashCode, byte[] value) {
        int start = (int) hashCode % modSize;
        if (start < 0) start += modSize;

        try {
            FileChannel channel = idxFile.getChannel();
            buffer.clear();

            // For now, we use local byte array for getting things into memory.
            // Now we need to go through at most numOfCopies entries, since this is on
            // SSD, and we do not want to waste too much time here.
            channel.position(getPosition(start));
            int bytesRead = channel.read(buffer);
            int bytesReadAgain = 0;
            if (bytesRead != maxTries * entrySize) {
                channel.position(getPosition(0));
                bytesReadAgain += channel.read(buffer);
                bytesRead += bytesReadAgain;
            }

            if (bytesRead != maxTries * entrySize) {
                throw new RuntimeException("bytes read " + bytesRead + ":" + bytesReadAgain);
            }

            // We read  maxTries number of entries into system, so that we can match
            // the right entry.
            buffer.flip();
            for (int k = 0; k < maxTries; ++k) {
                long key_part = buffer.getLong();
                buffer.get(value, 0, valSize);
                if (key_part == hashCode) return true;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return false;
    }

    @Override
    public byte[] get(long hashKey) {
        if (getValue(hashKey, res)) return res;
        return null;
    }

    public void close() throws IOException {
        idxFile.close();
    }

    // Make sure we release the resource even we forget to close files.
    public void finalize() {
        try {
            idxFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
