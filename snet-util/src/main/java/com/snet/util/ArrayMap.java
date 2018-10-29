package com.snet.util;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class ArrayMap<V> {
	private static final Object[] EMPTY = {};
	private static final Map<String, DomainIntKeys> keysMap = new HashMap<>();

	public static final class DomainIntKeys {
		private final String domain;
		private final Map<String, IntKey> keyMap;
		private volatile int inc = 0;

		private DomainIntKeys(String domain) {
			this.domain = domain;
			this.keyMap = new HashMap<>();
			this.inc = 0;
		}

		public final IntKey get(final String key) {
			IntKey hr = keyMap.get(key);
			if (hr == null)
				synchronized (keyMap) {
					if ((hr = keyMap.get(key)) == null)
						keyMap.put(key, hr = new IntKey(domain, key, inc++));
				}
			return hr;
		}
	}

	public static final class IntKey {
		public final String domain;
		public final String key;
		public final int idx;

		private IntKey(String domain, String key, int idx) {
			this.domain = domain;
			this.key = key;
			this.idx = idx;
		}
	}

	public static final DomainIntKeys getKeys(String domain) {
		DomainIntKeys keys = keysMap.get(domain);
		if (keys == null)
			synchronized (keysMap) {
				if ((keys = keysMap.get(domain)) == null)
					keysMap.put(domain, keys = new DomainIntKeys(domain));
			}
		return keys;
	}

	protected final int stepLength;
	protected volatile V[] array;
	protected volatile int capacity;

	public ArrayMap() {
		this(4, 4);
	}

	public ArrayMap(int initLength) {
		this(initLength, initLength);
	}

	public ArrayMap(int initLength, int stepLength) {
		initLength = initLength > 0 ? initLength : 0;
		stepLength = stepLength > 0 ? stepLength : 1;

		this.stepLength = stepLength < 8192 ? stepLength : 8192;
		this.array = (V[]) (initLength == 0 ? EMPTY : new Object[initLength]);
		this.capacity = array.length;
	}

	public final V get(final IntKey key) {
		return get(key.idx);
	}

	public final V get(final int key) {
		V[] array = this.array;
		if (key < array.length)
			return array[key];
		return null;
	}

	public final V set(final IntKey key, final V value) {
		return set(key.idx, value);
	}

	public final V set(final int key, final V value) {
		checkCapacity(key + 1);
		V[] array = this.array;
		V tmp = array[key];
		array[key] = value;
		return tmp;
	}

	public final V setIfAbsent(final IntKey key, final V value) {
		return setIfAbsent(key.idx, value);
	}

	public final V setIfAbsent(final int key, final V value) {
		checkCapacity(key + 1);
		V[] array = this.array;
		V tmp = array[key];
		if (tmp == null)
			array[key] = value;
		return tmp;
	}

	public final V safeSet(final IntKey key, final V value) {
		return safeSet(key.idx, value);
	}

	public final V safeSet(final int key, final V value) {
		syncCheckCapacity(key + 1);
		while (true) {
			V[] array = this.array;
			V tmp = array[key];
			array[key] = value;
			if (array.length == this.capacity)
				return tmp == value ? null : value;
		}
	}

	public final V safeSetIfAbsent(final IntKey key, final V value) {
		return safeSetIfAbsent(key.idx, value);
	}

	public final V safeSetIfAbsent(final int key, final V value) {
		syncCheckCapacity(key + 1);
		while (true) {
			V[] array = this.array;
			V tmp = array[key];
			if (tmp != null)
				return tmp;
			array[key] = value;
			if (array.length == this.capacity)
				return value;
		}
	}

	public final V remove(final IntKey key) {
		return remove(key.idx);
	}

	public final V remove(final int key) {
		V[] array = this.array;
		if (key < array.length) {
			V tmp = array[key];
			while (true) {
				array[key] = null;
				if (array.length == this.capacity)
					return tmp;
				array = this.array;
			}
		} else
			return null;
	}

	public final void clear() {
		while (true) {
			V[] array = this.array;
			for (int i = 0, len = array.length; i < len; ++i)
				array[i] = null;
			if (array == this.array)
				return;
		}
	}

	public final int capacity() {
		return array.length;
	}

	public Object[] array() {
		return array;
	}

	public final V rawget(final int idx) {
		return array[idx];
	}

	public final Enumeration<V> enumeration() {
		return new EnumIt();
	}

	private final void checkCapacity(int len) {
		if (len > capacity) {
			V[] array = this.array;
			V[] newArray = (V[]) new Object[getNewCapacity(len, stepLength)];
			capacity = newArray.length;
			System.arraycopy(array, 0, newArray, 0, array.length);
			this.array = newArray;
		}
	}

	private final void syncCheckCapacity(int len) {
		if (len > capacity)
			synchronized (this) {
				checkCapacity(len);
			}
	}

	static final int getNewCapacity(final int length, final int stepLength) {
		int newC = (length / stepLength) * stepLength;
		if (newC < length)
			newC += stepLength;
		return newC;
	}

	private final class EnumIt implements Enumeration<V> {
		private int i;
		private V value;

		public EnumIt() {
			this.i = -1;
			value = findNext();
		}

		@Override
		public boolean hasMoreElements() {
			return value != null;
		}

		@Override
		public V nextElement() {
			V tmp = value;
			value = findNext();
			return tmp;
		}

		private final V findNext() {
			final V[] values = array;
			final int length = values.length;
			while (++i < length)
				if ((values[i]) != null)
					return values[i];
			return null;
		}
	}
}
