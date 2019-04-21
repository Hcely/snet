package com.snet.util.coll;

import com.snet.Clearable;

import java.util.Iterator;
import java.util.Objects;

@SuppressWarnings("unchecked")
public class IntHashMap<V> extends AbsHashMap<V> implements Iterable<IntEntry<V>>, Clearable {
	public static int hash(int hashCode) {
		return hashCode ^ (hashCode >>> 16);
	}

	protected static class IntNodeEntry<V> extends NodeEntry<V> implements IntEntry<V> {
		protected final int key;
		protected IntNodeEntry<V> next;

		public IntNodeEntry(int hash, int key) {
			super(hash);
			this.key = key;
		}

		@Override
		public int getKey() {
			return key;
		}

		@Override
		public IntNodeEntry<V> getNext() {
			return next;
		}

		@Override
		public void setNext(NodeEntry<V> next) {
			this.next = (IntNodeEntry<V>) next;
		}
	}

	protected IntNodeEntry<V>[] tables;

	public IntHashMap() {
		this(DEF_INIT_CAPACITY, DEF_FACTOR);
	}

	public IntHashMap(int initCapacity) {
		this(initCapacity, DEF_FACTOR);
	}

	public IntHashMap(int initCapacity, double factor) {
		super(initCapacity, factor);
	}


	@Override
	protected NodeEntry<V>[] getTables() {
		return tables;
	}

	@Override
	protected NodeEntry<V>[] newTables(int capacity) {
		return new IntNodeEntry[capacity];
	}

	@Override
	protected void setTables(NodeEntry<V>[] tables) {
		this.tables = (IntNodeEntry<V>[]) tables;
	}

	public IntEntry<V> getEntity(int key) {
		return getEntity(key, false);
	}

	public IntEntry<V> getEntity(int key, boolean absentCreate) {
		final int hash = hash(key);
		final int idx = hash & (tables.length - 1);
		IntNodeEntry<V> node = tables[idx];
		for (; node != null; node = node.next) {
			if (node.key == key)
				return node;
		}
		if (absentCreate) {
			node = createNode(hash, key);
			addNode(tables, idx, node);
		}
		return node;
	}

	protected IntNodeEntry<V> createNode(int hash, int key) {
		return new IntNodeEntry<>(hash, key);
	}

	public IntEntry<V> removeEntity(int key) {
		return removeEntity(key, null, false);
	}

	public IntEntry<V> removeEntity(int key, Object value) {
		return removeEntity(key, value, true);
	}

	protected IntEntry<V> removeEntity(int key, Object value, boolean equalValue) {
		final int idx = hash(key) & (tables.length - 1);
		for (IntNodeEntry<V> node = tables[idx], prev = null; node != null; prev = node, node = node.next) {
			if (node.key == key) {
				if (equalValue && !Objects.equals(node.value, value))
					return null;
				removeNode(tables, idx, prev, node);
				return node;
			}
		}
		return null;
	}

	public V get(int key) {
		IntEntry<V> node = getEntity(key);
		return node == null ? null : node.getValue();
	}

	public V getOrDefault(int key, V defaultValue) {
		IntEntry<V> node = getEntity(key);
		return node == null ? defaultValue : node.getValue();
	}

	public V put(int key, V value) {
		IntEntry<V> node = getEntity(key);
		return node.setValue(value);
	}

	public V putIfAbsent(int key, V value) {
		IntEntry<V> node = getEntity(key);
		return node == null ? put(key, value) : node.getValue();
	}


	public boolean replace(int key, V oldValue, V newValue) {
		IntEntry<V> node = getEntity(key);
		return node != null && node.compareAndSetValue(oldValue, newValue);
	}

	public V replace(int key, V value) {
		IntEntry<V> node = getEntity(key);
		return node == null ? null : node.setValue(value);
	}


	public V remove(int key) {
		IntEntry<V> node = removeEntity(key);
		return node == null ? null : node.getValue();
	}

	public boolean remove(int key, Object value) {
		return removeEntity(key, value) != null;
	}

	@Override
	public Iterator<IntEntry<V>> iterator() {
		return new It(this);
	}


	protected static class It<V> extends AbsIt<IntNodeEntry<V>, IntNodeEntry<V>> {
		public It(IntHashMap<V> map) {
			super(map);
		}

		@Override
		protected IntNodeEntry<V> get(IntNodeEntry<V> node) {
			return node;
		}

		@Override
		protected void removeNode(IntNodeEntry<V> node) {
			((IntHashMap<V>) map).remove(node.key);
		}
	}
}
