package com.misingularity.util;

import java.io.IOException;
import java.io.RandomAccessFile;

import com.misingularity.util.MathUtils;

/*
 * created by ScennyMao on 12/05/14
 */
public class SSDHashTable {
	
	private RandomAccessFile datFile = null; 
	private RandomAccessFile sgnFile = null;
	
	final private long ratio = 2;
	
	private long modSize;
	private long datMetaSize;
	private long sgnMetaSize;

	public SSDHashTable(final String fileName, final String mode, final long size) {
		modSize = MathUtils.nextPrime(size * ratio);
		datMetaSize = Long.SIZE / 8;
		sgnMetaSize = Byte.SIZE / 8;
		try {
			datFile = new RandomAccessFile(fileName + "_table.dat", mode);
			sgnFile = new RandomAccessFile(fileName + "_table.sgn", mode);
			if (mode.indexOf('w') >= 0) {
				sgnFile.seek(0);
				for (int i = 0; i < modSize; ++i) {
					sgnFile.writeBoolean(false);
				}
			}
		} catch (IOException e) {
            e.printStackTrace();
		}
	}
	
	public void writeIn(long p, final long v) {
		try {
			p = ((p % modSize) + modSize) % modSize; /* make sure p is in the range */
			datFile.seek(p * datMetaSize);
			sgnFile.seek(p * sgnMetaSize);
			datFile.writeLong(v);
			sgnFile.writeBoolean(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public long find(long p, final long v) {
		try {
			p = ((p % modSize) + modSize) % modSize; /* make sure p is in the range */
			for (long i = p, k = 0; k < modSize; ++k) {
				sgnFile.seek(i * sgnMetaSize);
				boolean curSgn = sgnFile.readBoolean();
				if (curSgn == false) {
					return -(i + 1);
				}
				datFile.seek(i * datMetaSize);
				long curDat = datFile.readLong();
				if (curDat == v) {
					return i;
				}
				i = (i + 1) % modSize;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		/* should not reach here */
		assert false;
		return -1;
	}
}
