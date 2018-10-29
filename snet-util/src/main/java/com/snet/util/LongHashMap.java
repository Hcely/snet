package com.snet.util;

import java.util.Iterator;

import com.snet.Clearable;

@SuppressWarnings("unchecked")
public class LongHashMap<V> implements Clearable {
	public static final double DEF_FACTOR = 0.75;
	public static final int MAX_TABLE_CAPACITY = 1 << 20;

	public static final class Node<V> {
		protected final long key;
		protected V value;
		protected Node<V> next;

		private Node(long key, V value) {
			this.key = key;
			this.value = value;
		}

		public final V getValue() {
			return value;
		}

		public final V setValue(V value) {
			V tmp = this.value;
			this.value = value;
			return tmp;
		}

		public final long getKey() {
			return key;
		}

		public final V remove() {
			V tmp = value;
			value = null;
			next = null;
			return tmp;
		}
	}

	protected final int factor;
	protected Node<V>[] tables;
	protected int capacity;
	protected int mask;
	protected int threahold;
	protected int size;

	public LongHashMap() {
		this(16);
	}

	public LongHashMap(int initSize) {
		this(initSize, DEF_FACTOR);
	}

	public LongHashMap(int initSize, double factor) {
		initSize = IntHashMap.ceil2(initSize > 16 ? initSize : 16);
		this.factor = IntHashMap.camp((int) (factor * 128), 32, 128);
		this.tables = new Node[initSize];
		this.capacity = initSize;
		this.mask = initSize - 1;
		this.threahold = getThreahold(initSize);
		this.size = 0;
	}

	private final void checkThreahold(final int size) {
		if (size > threahold && capacity < MAX_TABLE_CAPACITY)
			incTable();
	}

	private final void incTable() {
		final int length = tables.length << 1;
		final Node<V>[] newTables = new Node[length];
		final int newMask = length - 1;

		for (Node<V> node : tables) {
			if (node == null)
				continue;
			int idx = (int) (node.key & newMask);
			Node<V> prev = node;
			Node<V> next = node.next;
			while (true) {
				if (next == null) {
					prev.next = newTables[idx];
					newTables[idx] = node;
					break;
				}
				int nextIdx = (int) (next.key & newMask);
				if (idx == nextIdx) {
					prev = next;
					next = next.next;
				} else {
					prev.next = newTables[idx];
					newTables[idx] = node;
					idx = nextIdx;
					prev = node = next;
					next = next.next;
				}
			}
		}

		this.tables = newTables;
		this.capacity = length;
		this.mask = newMask;
		this.threahold = getThreahold(length);
	}

	private final Node<V> addNode(final long key, final V value) {
		checkThreahold(++size);
		final Node<V> node = new Node<>(key, value);
		final int idx = (int) (key & mask);
		node.next = tables[idx];
		return tables[idx] = node;
	}

	private final V removeNode(final Node<V>[] tables, final int idx, final Node<V> prev, final Node<V> node) {
		--size;
		if (prev == null)
			tables[idx] = node.next;
		else
			prev.next = node.next;
		return node.remove();
	}

	public V put(final long key, final V value) {
		for (Node<V> node = tables[(int) (key & mask)]; node != null; node = node.next)
			if (node.key == key)
				return node.setValue(value);
		addNode(key, value);
		return null;
	}

	public V putIfAbsent(final long key, final V value) {
		for (Node<V> node = tables[(int) (key & mask)]; node != null; node = node.next)
			if (node.key == key)
				return node.value;
		addNode(key, value);
		return null;
	}

	public V get(final long key) {
		for (Node<V> node = tables[(int) (key & mask)]; node != null; node = node.next)
			if (node.key == key)
				return node.value;
		return null;
	}

	public V replace(final long key, final V value) {
		for (Node<V> node = tables[(int) (key & mask)]; node != null; node = node.next)
			if (node.key == key)
				return node.setValue(value);
		return null;
	}

	public boolean replace(final long key, final V oldValue, final V newValue) {
		for (Node<V> node = tables[(int) (key & mask)]; node != null; node = node.next)
			if (node.key == key)
				if (node.value == null) {
					if (oldValue == null) {
						node.value = newValue;
						return true;
					} else
						return false;
				} else if (node.value.equals(oldValue)) {
					node.value = newValue;
					return true;
				} else
					return false;
		return false;
	}

	public V remove(final long key) {
		final int idx = (int) (key & mask);
		final Node<V>[] tables = this.tables;
		for (Node<V> node = tables[idx], prev = null; node != null;)
			if (node.key == key)
				return removeNode(tables, idx, prev, node);
			else {
				prev = node;
				node = node.next;
			}
		return null;
	}

	public boolean remove(final long key, final V value) {
		final int idx = (int) (key & mask);
		final Node<V>[] tables = this.tables;
		for (Node<V> node = tables[idx], prev = null; node != null;)
			if (node.key == key) {
				if (node.value == null) {
					if (value == null) {
						removeNode(tables, idx, prev, node);
						return true;
					} else
						return false;
				} else if (node.value.equals(value)) {
					removeNode(tables, idx, prev, node);
					return true;
				} else
					return false;
			} else {
				prev = node;
				node = node.next;
			}
		return false;
	}

	public boolean containsKey(final long key) {
		for (Node<V> node = tables[(int) (key & mask)]; node != null; node = node.next)
			if (node.key == key)
				return true;
		return false;
	}

	public Iterator<Node<V>> iterator() {
		return new It<>(this);
	}

	public int size() {
		return size;
	}

	public void clear() {
		final Node<V>[] tables = this.tables;
		for (int i = 0, len = tables.length; i < len; ++i) {
			for (Node<V> node = tables[i], tmp; node != null;) {
				node.value = null;
				tmp = node.next;
				node.next = null;
				node = tmp;
			}
			tables[i] = null;
		}
		size = 0;
	}

	private final int getThreahold(final int capacity) {
		return (capacity * factor) >>> 7;
	}

	private static final class It<V> implements Iterator<Node<V>> {
		private final LongHashMap<V> map;
		private final Node<V>[] tables;
		private int idx;
		private Node<V> prev;
		private Node<V> node;

		public It(LongHashMap<V> map) {
			this.map = map;
			this.tables = map.tables;
			this.idx = -1;
			next();
		}

		@Override
		public final boolean hasNext() {
			return node != null;
		}

		@Override
		public final Node<V> next() {
			prev = node;
			if (node == null || (node = node.next) == null)
				do {
					if (++idx < tables.length)
						node = tables[idx];
					else
						break;
				} while (node == null);
			return prev;
		}

		@Override
		public final void remove() {
			map.remove(prev.key);
		}

	}
}
