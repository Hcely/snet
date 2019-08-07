package com.snet.util.coll;

import com.snet.Clearable;

import java.util.Iterator;
import java.util.Objects;

@SuppressWarnings("unchecked")
public class LongHashMap<V> extends AbsHashMap<V> implements Iterable<LongEntry<V>>, Clearable {
	public static int hash(long hashCode) {
		return (int) (hashCode ^ (hashCode >>> 32));
	}

	protected static class LongNodeEntry<V> extends NodeEntry<V> implements LongEntry<V> {
		protected final long key;
		protected LongNodeEntry<V> next;

		public LongNodeEntry(int hash, long key) {
			super(hash);
			this.key = key;
		}

		@Override
		public long getKey() {
			return key;
		}

		@Override
		public LongNodeEntry<V> getNext() {
			return next;
		}

		@Override
		public void setNext(NodeEntry<V> next) {
			this.next = (LongNodeEntry<V>) next;
		}
	}

	protected LongNodeEntry<V>[] tables;

	public LongHashMap() {
		this(DEF_INIT_CAPACITY, DEF_FACTOR);
	}

	public LongHashMap(int initCapacity) {
		this(initCapacity, DEF_FACTOR);
	}

	public LongHashMap(int initCapacity, double factor) {
		super(initCapacity, factor);
	}


	@Override
	protected NodeEntry<V>[] getTables() {
		return tables;
	}

	@Override
	protected NodeEntry<V>[] newTables(int capacity) {
		return new LongNodeEntry[capacity];
	}

	@Override
	protected void setTables(NodeEntry<V>[] tables) {
		this.tables = (LongNodeEntry<V>[]) tables;
	}

	public LongEntry<V> getEntity(long key) {
		return getEntity(key, false);
	}

	public LongEntry<V> getEntity(long key, boolean absentCreate) {
		final int hash = hash(key);
		final int idx = hash & (tables.length - 1);
		LongNodeEntry<V> node = tables[idx];
		for (; node != null; node = node.next) {
			if (node.key == key)
				return node;
		}
		if (absentCreate) {
			addNode(tables, idx, node = createNode(hash, key));
		}
		return node;
	}

	protected LongNodeEntry<V> createNode(int hash, long key) {
		return new LongNodeEntry<>(hash, key);
	}

	public LongEntry<V> removeEntity(long key) {
		return removeEntity(key, null, false);
	}

	public LongEntry<V> removeEntity(long key, Object value) {
		return removeEntity(key, value, true);
	}

	protected LongEntry<V> removeEntity(long key, Object value, boolean equalValue) {
		final int idx = hash(key) & (tables.length - 1);
		for (LongNodeEntry<V> node = tables[idx], prev = null; node != null; prev = node, node = node.next) {
			if (node.key == key) {
				if (equalValue && !Objects.equals(node.value, value))
					return null;
				removeNode(tables, idx, prev, node);
				return node;
			}
		}
		return null;
	}

	public V get(long key) {
		LongEntry<V> node = getEntity(key);
		return node == null ? null : node.getValue();
	}

	public V getOrDefault(long key, V defaultValue) {
		LongEntry<V> node = getEntity(key);
		return node == null ? defaultValue : node.getValue();
	}

	public V put(long key, V value) {
		LongEntry<V> node = getEntity(key);
		return node.setValue(value);
	}

	public V putIfAbsent(long key, V value) {
		LongEntry<V> node = getEntity(key);
		return node == null ? put(key, value) : node.getValue();
	}


	public boolean replace(long key, V oldValue, V newValue) {
		LongEntry<V> node = getEntity(key);
		return node != null && node.compareAndSetValue(oldValue, newValue);
	}

	public V replace(long key, V value) {
		LongEntry<V> node = getEntity(key);
		return node == null ? null : node.setValue(value);
	}


	public V remove(long key) {
		LongEntry<V> node = removeEntity(key);
		return node == null ? null : node.getValue();
	}

	public boolean remove(long key, Object value) {
		return removeEntity(key, value) != null;
	}

	@Override
	public Iterator<LongEntry<V>> iterator() {
		return new It(this);
	}


	protected static class It<V> extends AbsIt<LongEntry<V>, LongNodeEntry<V>> {
		public It(LongHashMap<V> map) {
			super(map);
		}

		@Override
		protected LongNodeEntry<V> get(LongNodeEntry<V> node) {
			return node;
		}

		@Override
		protected void removeNode(LongNodeEntry<V> node) {
			((LongHashMap<V>) map).remove(node.key);
		}
	}
}
