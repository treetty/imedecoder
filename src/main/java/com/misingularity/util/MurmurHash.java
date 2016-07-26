package com.misingularity.util;

/**
 * Taken/modified from
 * http://d3s.mff.cuni.cz/~holub/sw/javamurmurhash/MurmurHash.java
 * 
 */
public final class MurmurHash
{
    public static long getTime = 0;
    public static long getNum = 0;

    public static void setTime() { 
        getTime = 0; 
        getNum = 0;
    }

	/**
	 * Generates 32 bit hash from byte array of the given length and seed.
	 * 
	 * @param data
	 *            int array to hash
	 * @param length
	 *            length of the array to hash
	 * @param seed
	 *            initial seed value
	 * @return 32 bit hash of the given array
	 */
	public static int hash32(final int[] data, int startPos, int endPos, int seed) {
		// 'm' and 'r' are mixing constants generated offline.
		// They're not really 'magic', they just happen to work well.
		final int m = 0x5bd1e995;
		final int r = 24;
		final int length = endPos - startPos;
		// Initialize the hash to a random value
		int h = seed ^ length;

		for (int i = startPos; i < endPos; i++) {
			int k = data[i];
			k *= m;
			k ^= k >>> r;
			k *= m;
			h *= m;
			h ^= k;
		}

		h ^= h >>> 13;
		h *= m;
		h ^= h >>> 15;

		return h;
	}

	public static int hash32(final int[] data, int startPos, int endPos) {
		return hash32(data, startPos, endPos, 0x9747b28c);
	}

	public static long hashOneLong(final long k_, final int seed) {
		long k = k_;
		final long m = 0xc6a4a7935bd1e995L;
		final int r = 47;

		long h = (seed & 0xffffffffl) ^ (1 * m);

		k *= m;
		k ^= k >>> r;
		k *= m;

		h ^= k;
		h *= m;

		h ^= h >>> r;
		h *= m;
		h ^= h >>> r;

		return h;
	}
	
	public static long hashTwoLongs(final long k1, final long k2) {
		final int seed = 0x9747b28c;
		final long m = 0xc6a4a7935bd1e995L;
		final int r = 47;

		long h = (seed & 0xffffffffl) ^ (1 * m);
		for (int i = 0; i <= 1; ++i) {
			long k = -1;
			switch (i) {
				case 0:
					k = k1;
					break;
				case 1:
					k = k2;
					break;
				default:
					assert false;
			}
			k *= m;
			k ^= k >>> r;
			k *= m;

			h ^= k;
			h *= m;
		}

		h ^= h >>> r;
		h *= m;
		h ^= h >>> r;
		
		return h;
	}

	public static long hashThreeLongs(final long k1, final long k2, final long k3) {

		final int seed = 0x9747b28c;
		final long m = 0xc6a4a7935bd1e995L;
		final int r = 47;

		long h = (seed & 0xffffffffl) ^ (1 * m);
		for (int i = 0; i <= 2; ++i) {
			long k = -1;
			switch (i) {
				case 0:
					k = k1;
					break;
				case 1:
					k = k2;
					break;
				case 2:
					k = k3;
					break;
				default:
					assert false;
			}
			k *= m;
			k ^= k >>> r;
			k *= m;

			h ^= k;
			h *= m;
		}

		h ^= h >>> r;
		h *= m;
		h ^= h >>> r;

		return h;
	}

    /**
     * Generates 32 bit hash from byte array of the given startPos, endPos
     * and seed.
     *
     * @param data byte array to hash
     * @param startPos start position
     * @param endPos end position
     * @param seed initial seed value
     * @return 32 bit hash of the given array
     */
    public static int byteArrayHash32(final byte[] data, int startPos, int endPos, int seed) {
        // 'm' and 'r' are mixing constants generated offline.
        // They're not really 'magic', they just happen to work well.
        final int m = 0x5bd1e995;
        final int r = 24;

        int len = endPos - startPos;
        int h = seed^len;
        int k = 0, cnt = 0;
        for (int i = startPos; i < endPos; i++) {
            k = k | ((data[i] & 0xff) << cnt);
            cnt += 8;
            if (((i - startPos) & 3) == 3) {
                k *= m;
                k ^= k >>> r;
                k *= m;
                h *= m;
                h ^= k;
                k = cnt = 0;
            }
        }
        if ((len & 3) != 0) {
            h ^= k;
            h *= m; 
        }

        h ^= h >>> 13;
        h *= m;
        h ^= h >>> 15;

        return h;
    }

    /**
     * Generates 32 bit hash from byte array with default seed value given startPos
     * and endPos.
     *
     * @param data byte array to hash
     * @param startPos start position
     * @param endPos end position
     * @return 32 bit hash of the given array
     */
    public static int byteArrayHash32(final byte[] data, int startPos, int endPos) {
        return byteArrayHash32(data, startPos, endPos, 0x9747b28c);
    }

    /**
     * Generates 32 bit hash from byte array of the given length and
     * seed.
     *
     * @param data byte array to hash
     * @param length length of the array to hash
     * @param seed initial seed value
     * @return 32 bit hash of the given array
     */
    public static int hash32(final byte[] data, int length, int seed) {
        // 'm' and 'r' are mixing constants generated offline.
        // They're not really 'magic', they just happen to work well.
        final int m = 0x5bd1e995;
        final int r = 24;

        // Initialize the hash to a random value
        int h = seed^length;
        int length4 = length/4;

        for (int i=0; i<length4; i++) {
            final int i4 = i*4;
            int k = (data[i4+0]&0xff) +((data[i4+1]&0xff)<<8)
                    +((data[i4+2]&0xff)<<16) +((data[i4+3]&0xff)<<24);
            k *= m;
            k ^= k >>> r;
            k *= m;
            h *= m;
            h ^= k;
        }

        // Handle the last few bytes of the input array
        switch (length%4) {
            case 3: h ^= (data[(length&~3) +2]&0xff) << 16;
            case 2: h ^= (data[(length&~3) +1]&0xff) << 8;
            case 1: h ^= (data[length&~3]&0xff);
                h *= m;
        }

        h ^= h >>> 13;
        h *= m;
        h ^= h >>> 15;

        return h;
    }

    /**
     * Generates 32 bit hash from byte array with default seed value.
     *
     * @param data byte array to hash
     * @param length length of the array to hash
     * @return 32 bit hash of the given array
     */
    public static int hash32(final byte[] data, int length) {
        return hash32(data, length, 0x9747b28c);
    }

    /**
     * Generates 32 bit hash from a string.
     *
     * @param text string to hash
     * @return 32 bit hash of the given string
     */
    public static int hash32(final String text) {
        long tmp = System.currentTimeMillis();
        final byte[] bytes = text.getBytes();
        ++getNum;
        getTime += System.currentTimeMillis() - tmp;
        return hash32(bytes, bytes.length);
    }

    /**
     * Generates 32 bit hash from a substring.
     *
     * @param text string to hash
     * @param from starting index
     * @param length length of the substring to hash
     * @return 32 bit hash of the given string
     */
    public static int hash32(final String text, int from, int length) {
        return hash32(text.substring( from, from+length));
    }

        /**
     * Generates 64 bit hash from byte array of the given startPos, endPos
     * and seed.
     *
     * @param data byte array to hash
     * @param startPos start position
     * @param endPos end position
     * @param seed initial seed value
     * @return 64 bit hash of the given array
     */
    public static long byteArrayHash64(final byte[] data, int startPos, int endPos, int seed) {
        final long m = 0xc6a4a7935bd1e995L;
        final int r = 47;

        int len = endPos - startPos;
        long h = (seed&0xffffffffl)^(len*m);

        long k = 0;
        int cnt = 0;
        for (int i = startPos; i < endPos; i++) {
            k = k | ((long)(data[i] & 0xff) << cnt);
            cnt += 8;
            if (((i - startPos) & 7) == 7) {
                k *= m;
                k ^= k >>> r;
                k *= m;

                h ^= k;
                h *= m;
                k = 0;
                cnt = 0;
            }
        }

        if ((len & 7) != 0) {
            h ^= k;
            h *= m;
        }

        h ^= h >>> r;
        h *= m;
        h ^= h >>> r;

        return h;
    }

    /**
     * Generates 64 bit hash from byte array with default seed value given startPos
     * and endPos.
     *
     * @param data byte array to hash
     * @param startPos start position
     * @param endPos end position
     * @return 64 bit hash of the given array
     */
    public static long byteArrayHash64(final byte[] data, int startPos, int endPos) {
        return byteArrayHash64(data, startPos, endPos, 0xe17a1465);
    }

    /**
     * Generates 64 bit hash from byte array of the given length and seed.
     *
     * @param data byte array to hash
     * @param length length of the array to hash
     * @param seed initial seed value
     * @return 64 bit hash of the given array
     */
    public static long hash64(final byte[] data, int length, int seed) {
        final long m = 0xc6a4a7935bd1e995L;
        final int r = 47;

        long h = (seed&0xffffffffl)^(length*m);

        int length8 = length/8;

        for (int i=0; i<length8; i++) {
            final int i8 = i*8;
            long k =  ((long)data[i8+0]&0xff)      +(((long)data[i8+1]&0xff)<<8)
                    +(((long)data[i8+2]&0xff)<<16) +(((long)data[i8+3]&0xff)<<24)
                    +(((long)data[i8+4]&0xff)<<32) +(((long)data[i8+5]&0xff)<<40)
                    +(((long)data[i8+6]&0xff)<<48) +(((long)data[i8+7]&0xff)<<56);

            k *= m;
            k ^= k >>> r;
            k *= m;

            h ^= k;
            h *= m;
        }

        switch (length%8) {
            case 7: h ^= (long)(data[(length&~7)+6]&0xff) << 48;
            case 6: h ^= (long)(data[(length&~7)+5]&0xff) << 40;
            case 5: h ^= (long)(data[(length&~7)+4]&0xff) << 32;
            case 4: h ^= (long)(data[(length&~7)+3]&0xff) << 24;
            case 3: h ^= (long)(data[(length&~7)+2]&0xff) << 16;
            case 2: h ^= (long)(data[(length&~7)+1]&0xff) << 8;
            case 1: h ^= (long)(data[length&~7]&0xff);
                h *= m;
        };

        h ^= h >>> r;
        h *= m;
        h ^= h >>> r;

        return h;
    }

    /**
     * Generates 64 bit hash from byte array with default seed value.
     *
     * @param data byte array to hash
     * @param length length of the array to hash
     * @return 64 bit hash of the given string
     */
    public static long hash64(final byte[] data, int length) {
        return hash64(data, length, 0xe17a1465);
    }

    /**
     * Generates 64 bit hash from a string.
     *
     * @param text string to hash
     * @return 64 bit hash of the given string
     */
    public static long hash64(final String text) {
        final byte[] bytes = text.getBytes();
        return hash64(bytes, bytes.length);
    }

    /**
     * Generates 64 bit hash from a substring.
     *
     * @param text string to hash
     * @param from starting index
     * @param length length of the substring to hash
     * @return 64 bit hash of the given array
     */
    public static long hash64(final String text, int from, int length) {
        return hash64(text.substring( from, from+length));
    }
}