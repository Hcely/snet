package com.snet.util;

import java.util.Arrays;

public class Bitmap {
	private static final long L = 0xFFFF_FFFF_FFFF_FFFFL;

	public static final long getMask(int off) {
		return 1L << (63 - off);
	}

	public static final long getMask(int off, int len) {
		len = 64 - len;
		return (L >>> len) << (len - off);
	}

	public static final boolean get(final long value, final long mask) {
		return (value & mask) == mask;
	}

	public static final boolean equal(final long value, final long mask, final boolean b) {
		final long l = value & mask;
		return b ? (l == mask) : (l == 0);
	}

	public static final long set(final long value, final long mask, final boolean b) {
		return b ? (value | mask) : (value & ~mask);
	}

	public static final long and(final long value, final long mask, final boolean b) {
		return b ? (((value & mask) | ~mask) & value) : (value & ~mask);
	}

	public static final long or(final long value, final long mask) {
		return value | mask;
	}

	public static final long xor(final long value, final long mask) {
		return ((value ^ L) | ~mask) & value;
	}

	public static final long not(final long value, final long mask) {
		return ((~value) | ~mask) & value;
	}

	protected long[] map = RuntimeUtil.EMPTY_LONGS;
	protected int capacity = 0;

	public Bitmap() {
		this(64);
	}

	public Bitmap(int initCapacity) {
		ensureCapacity(initCapacity);
	}

	public void ensureCapacity(int needCapacity) {
		if (needCapacity > capacity) {
			int newCapacity = (needCapacity >>> 6) << 6;
			if (newCapacity < needCapacity)
				newCapacity += 64;
			this.map = Arrays.copyOf(map, newCapacity >>> 6);
			this.capacity = newCapacity;
		}
	}

	public boolean get(final int idx) {
		return idx < capacity && get(map[idx >>> 6], getMask(idx & 63));
	}

	public boolean equals(final int idx, final boolean b) {
		return get(idx) == b;
	}

	public boolean getSet(final int idx, final boolean b) {
		ensureCapacity(idx + 1);
		final int mapIdx = idx >>> 6;
		final long mask = getMask(idx & 63);
		final long value = map[mapIdx];
		final boolean old = get(value, mask);
		if (old != b)
			map[mapIdx] = set(value, mask, b);
		return old;
	}

	public void set(final int idx, final boolean b) {
		ensureCapacity(idx + 1);
		final int mapIdx = idx >>> 6;
		final long mask = getMask(idx & 63);
		final long value = map[mapIdx];
		map[mapIdx] = set(value, mask, b);
	}

	public boolean and(final int idx, final boolean b) {
		ensureCapacity(idx + 1);
		final int mapIdx = idx >>> 6;
		final long mask = getMask(idx & 63);
		final long value = map[mapIdx];
		final boolean old = get(value, mask);
		if (old == b)
			return b;
		map[mapIdx] = set(value, mask, false);
		return false;
	}

	public boolean or(final int idx, final boolean b) {
		ensureCapacity(idx + 1);
		final int mapIdx = idx >>> 6;
		final long mask = getMask(idx & 63);
		final long value = map[mapIdx];
		final boolean old = get(value, mask);
		if (old == b)
			return b;
		map[mapIdx] = set(value, mask, true);
		return true;
	}

	public boolean xor(final int idx, final boolean b) {
		ensureCapacity(idx + 1);
		final int mapIdx = idx >>> 6;
		final long mask = getMask(idx & 63);
		final long value = map[mapIdx];
		final boolean newB = get(value, mask) != b;
		map[mapIdx] = set(value, mask, newB);
		return newB;
	}

	public boolean not(final int idx) {
		ensureCapacity(idx + 1);
		final int mapIdx = idx >>> 6;
		final long mask = getMask(idx & 63);
		final long value = map[mapIdx];
		final boolean newB = !get(value, mask);
		map[mapIdx] = set(value, mask, newB);
		return newB;
	}

	public boolean equals(final int idx, int len, final boolean b) {
		if (idx + len > capacity)
			return false;
		final long[] map = this.map;
		for (int mapIdx = idx >>> 6, off = idx & 63, remain = 64 - off; len > 0; len -= remain, off = 0, remain = 64, ++mapIdx) {
			remain = remain < len ? remain : len;
			if (!equal(map[mapIdx], getMask(off, remain), b))
				return false;
		}
		return true;
	}

	public void set(final int idx, int len, final boolean b) {
		ensureCapacity(idx + len);
		final long[] map = this.map;
		for (int mapIdx = idx >>> 6, off = idx & 63, remain = 64 - off; len > 0; len -= remain, off = 0, remain = 64, ++mapIdx) {
			remain = remain < len ? remain : len;
			map[mapIdx] = set(map[mapIdx], getMask(off, remain), b);
		}
	}

	public void and(final int idx, int len, final boolean b) {
		ensureCapacity(idx + len);
		final long[] map = this.map;
		for (int mapIdx = idx >>> 6, off = idx & 63, remain = 64 - off; len > 0; len -= remain, off = 0, remain = 64, ++mapIdx) {
			remain = remain < len ? remain : len;
			map[mapIdx] = and(map[mapIdx], getMask(off, remain), b);
		}
	}

	public void or(final int idx, int len, final boolean b) {
		ensureCapacity(idx + len);
		if (!b)
			return;
		final long[] map = this.map;
		for (int mapIdx = idx >>> 6, off = idx & 63, remain = 64 - off; len > 0; len -= remain, off = 0, remain = 64, ++mapIdx) {
			remain = remain < len ? remain : len;
			map[mapIdx] = or(map[mapIdx], getMask(off, remain));
		}
	}

	public void xor(final int idx, int len, final boolean b) {
		ensureCapacity(idx + len);
		if (!b)
			return;
		final long[] map = this.map;
		for (int mapIdx = idx >>> 6, off = idx & 63, remain = 64 - off; len > 0; len -= remain, off = 0, remain = 64, ++mapIdx) {
			remain = remain < len ? remain : len;
			map[mapIdx] = xor(map[mapIdx], getMask(off, remain));
		}
	}

	public void not(final int idx, int len) {
		ensureCapacity(idx + len);
		final long[] map = this.map;
		for (int mapIdx = idx >>> 6, off = idx & 63, remain = 64 - off; len > 0; len -= remain, off = 0, remain = 64, ++mapIdx) {
			remain = remain < len ? remain : len;
			map[mapIdx] = not(map[mapIdx], getMask(off, remain));
		}
	}
}

