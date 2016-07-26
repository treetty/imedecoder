package com.misingularity.util;

import java.io.RandomAccessFile;

/**
 *  This is the interface for the serialize an ArrayList of items of type V.
 *  We choose to work with simpler interface, if there is io error inside, simply raise runtime error.
 * Created by xiaoyun on 12/7/14.
 */
public interface SSDByteTrieItemsSerializer<D> {
    // Dump the current Data list into Index file.
    public void saveToIndexFile(RandomAccessFile idxFile, SSDByteTrieItems<D> items);

    // Given the base position fo the index file, and number of branches, and index of branch, read in the
    // memory block for the leaf node.
    public SSDByteTrieItems<D> loadFromIndexFile(RandomAccessFile idxFile, long base, int numOfBranches, int idx);
}
