package com.misingularity.util;

import java.io.*;

/**
 * Created by xiaoyun on 7/16/14.
 */
public class SSDSSTable implements StringMap<byte[]> {

    public static String getIdxPrefix() { return ".idx"; }
    public static String getDatPrefix() { return ".dat"; }

    public static SSDSSTable load(String path) throws IOException {
        RandomAccessFile idxFile = new RandomAccessFile(path + SSDSSTable.getIdxPrefix(), "r");
        RandomAccessFile datFile = new RandomAccessFile(path + SSDSSTable.getDatPrefix(), "r");
        return new SSDSSTable(idxFile, datFile);
    }


    private static int metaSize = 4;

    private RandomAccessFile idxFile;
    private RandomAccessFile datFile;
    private int modSize;

    public SSDSSTable(RandomAccessFile i, RandomAccessFile d) throws IOException {
        idxFile = i;
        datFile = d;
        modSize = (int)idxFile.length()/metaSize;
    }

    public byte[] get(String key) {
        int idx = key.hashCode() % modSize;
        if (idx < 0) idx += modSize;

        try {
            idxFile.seek(idx * metaSize);

            int pos = idxFile.readInt();
            if (pos == -1) return null;

            datFile.seek(pos);
            String lkey = "";
            String lval = null;
            while (!lkey.equals(key)) {
                short ksize = datFile.readShort();
                if (ksize == -1) return null;

                byte[] ba_key = new byte[ksize];
                datFile.read(ba_key);
                lkey = new String(ba_key);

                int vsize = datFile.readInt();
                byte[] ba_val = new byte[vsize];
                datFile.read(ba_val);
                if (lkey.equals(key)) return ba_val;
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
            throw new RuntimeException(ioe);
        }
        return null;
    }

    public byte[] get(long hashKey) {
        throw new UnsupportedOperationException("Invalid operation for get method for hashKey");
    }

    public void close() throws IOException {
        idxFile.close();
        datFile.close();
    }


    // Make sure we release the resource even we forget to close files.
    public void finalize() {
        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
