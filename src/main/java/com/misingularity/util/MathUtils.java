package com.misingularity.util;

/*
 * created by ScennyMao on 12/05/14
 */

public final class MathUtils {
	
	private static boolean isPrime(final long v) {
		if (v == 1) return false;
		for (long i = 2; i < v; ++i)
		if (v % i == 0) return false;
		return true;
	}

	public static long nextPrime(long v) {
		++v;
		for (; !isPrime(v); ) v++;
		return v;
	}
}
