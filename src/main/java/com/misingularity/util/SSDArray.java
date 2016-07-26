package com.misingularity.util;

import java.io.IOException;
import java.io.RandomAccessFile;

/*
 * created by ScennyMao on 12/05/14
 */
public class SSDArray {
	
	private RandomAccessFile datFile = null; 
	
	private long datMetaSize;

	public SSDArray(final String fileName, final String mode) {
		datMetaSize = Long.SIZE / 8;
		try {
			datFile = new RandomAccessFile(fileName+"_array.dat", mode);
		} catch (IOException e) {
            e.printStackTrace();
		}
	}
	
	public long get(long pos) {
		try {
			datFile.seek(pos * datMetaSize * 2);
			long data = datFile.readLong();
			return data;
		} catch (Exception e) {
			e.printStackTrace();
		}
		assert false;
		return -1;
	}
	
	public void add(long w, long v) {
		try {
			datFile.writeLong(w);
			datFile.writeLong(v);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeIn(long pos, long w, long v) { 
		try {
			datFile.seek(pos * datMetaSize * 2);
			datFile.writeLong(w);
			datFile.writeLong(v);
		} catch (IOException e) {
			e.printStackTrace();	
		}
	}
}
