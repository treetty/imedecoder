package com.misingularity.util;

public class BitsUtils {
	private static final long INT_BITS_MASK = ((1L << Integer.SIZE) - 1);
	
	public static int getHighInt(long key) {
		return ((int) (key >>> Integer.SIZE));
	}
	
	public static int getLowInt(long key) {
		return (int)(key & INT_BITS_MASK);
	}

	public static long combineInts(int lowInt, int highInt) {
		return (((long) highInt) << Integer.SIZE) | (lowInt & INT_BITS_MASK);
	}
}
