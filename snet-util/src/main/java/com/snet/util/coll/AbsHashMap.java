package com.snet.util.coll;

import com.snet.Clearable;
import com.snet.util.MathUtil;

import java.util.Iterator;
import java.util.Objects;

@SuppressWarnings("unchecked")
public abstract class AbsHashMap<V> implements Clearable {
	public static final double DEF_FACTOR = 4;
	public static final int DEF_INIT_CAPACITY = 16;
	public static final int MAX_TABLE_CAPACITY = 1 << 24;

	protected static abstract class NodeEntry<V> implements EntryValue<V> {
		protected final int hash;
		protected V value;

		public NodeEntry(int hash) {
			this.hash = hash;
		}


		@Override
		public V getValue() {
			return value;
		}

		@Override
		public V setValue(V value) {
			V tmp = this.value;
			this.value = value;
			return tmp;
		}

		public abstract NodeEntry<V> getNext();

		public abstract void setNext(NodeEntry<V> next);
	}

	protected final int initCapacity;
	protected final double factor;
	protected int size;
	protected int threshold;

	public AbsHashMap(int initCapacity, double factor) {
		this.factor = factor < 0.01 ? 0.01 : (factor < 256 ? factor : 256);
		this.initCapacity = Math.max(initCapacity, 4);
		reset();
	}

	protected abstract NodeEntry<V>[] getTables();

	protected abstract NodeEntry<V>[] newTables(int capacity);

	protected abstract void setTables(NodeEntry<V>[] tables);

	protected void rehashTable() {
		final NodeEntry<V>[] tables = getTables();
		final int newCapacity = tables.length << 1;
		final NodeEntry<V>[] newTables = new NodeEntry[newCapacity];
		final int newMask = newCapacity - 1;
		NodeEntry<V> start, end;
		for (int i = 0, len = tables.length, idx, prevIdx = -1; i < len; ++i, prevIdx = -1) {
			NodeEntry<V> node = tables[i];
			while ((start = end = node) != null) {
				idx = prevIdx == -1 ? (node.hash & newMask) : prevIdx;
				while ((node = node.getNext()) != null && (prevIdx = (node.hash & newMask)) == idx)
					end = node;
				end.setNext(newTables[idx]);
				newTables[idx] = start;
			}
			tables[i] = null;
		}
		this.setTables(newTables);
		this.threshold = getNewThreshold(newCapacity);
	}

	protected void addNode(NodeEntry<V>[] tables, int idx, NodeEntry<V> node) {
		node.setNext(tables[idx]);
		tables[idx] = node;
		if (++size > threshold) {
			rehashTable();
		}
	}

	protected void removeNode(NodeEntry<V>[] tables, int idx, NodeEntry<V> prev, NodeEntry<V> node) {
		if (prev == null)
			tables[idx] = node.getNext();
		else
			prev.setNext(node.getNext());
		--size;
	}

	protected int getNewThreshold(int capacity) {
		if (capacity < MAX_TABLE_CAPACITY) {
			int i = (int) (capacity * factor);
			return i < 0 ? Integer.MAX_VALUE : i;
		}
		return Integer.MAX_VALUE;
	}

	public int size() {
		return size;
	}

	public boolean containsValue(Object value) {
		if (size > 0) {
			for (NodeEntry<V> node : getTables()) {
				for (; node != null; node = node.getNext())
					if (Objects.equals(node.value, value))
						return true;
			}
		}
		return false;
	}

	@Override
	public void clear() {
		if (size > 0) {
			NodeEntry<V>[] tables = getTables();
			for (int i = 0, len = tables.length; i < len; ++i)
				tables[i] = null;
			this.size = 0;
		}
	}

	public void reset() {
		int capacity = MathUtil.ceil2((int) (initCapacity / factor));
		this.size = 0;
		this.setTables(newTables(capacity));
		this.threshold = getNewThreshold(capacity);
	}

	@SuppressWarnings("rawtypes")
	protected static abstract class AbsIt<E, N extends NodeEntry> implements Iterator<E> {
		protected final AbsHashMap map;
		private int idx;
		private N prev;
		private N node;

		public AbsIt(AbsHashMap map) {
			this.map = map;
			this.idx = 0;
			nextNode();
		}


		@Override
		public boolean hasNext() {
			return node != null;
		}

		@Override
		public E next() {
			prev = node;
			nextNode();
			return get(prev);
		}

		private void nextNode() {
			NodeEntry[] tables = map.getTables();
			do {
				if (node != null)
					node = (N) node.getNext();
				else if (idx < tables.length)
					node = (N) tables[idx++];
				else
					break;
			} while (node == null);
		}

		@Override
		public void remove() {
			removeNode(prev);
		}

		protected abstract E get(N node);

		protected abstract void removeNode(N node);
	}

}
