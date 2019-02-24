package com.snet.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SNetHashMap<K, V> implements MapPlus<K, V> {
	public static final double DEF_FACTOR = 4;
	public static final int DEF_INIT_CAPACITY = 16;
	protected static int MAX_TABLE_CAPACITY = 1 << 24;

	public static void setMaxTableCapacity(int maxTableCapacity) {
		MAX_TABLE_CAPACITY = maxTableCapacity;
	}


	protected static class HashNode<K, V> implements EntryPlus<K, V> {
		protected final int hash;
		protected final K key;
		protected V value;
		protected HashNode<K, V> next;

		public HashNode(int hash, K key) {
			this.hash = hash;
			this.key = key;
		}

		@Override
		public K getKey() {
			return key;
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

		@Override
		public String toString() {
			return key + "=" + value;
		}
	}


	protected final double factor;
	protected final int initCapacity;
	protected KeyEqualFunc<K> keyEqualFunc;

	protected int size;
	protected int threshold;
	protected HashNode<K, V>[] tables;

	public SNetHashMap() {
		this(DEF_INIT_CAPACITY);
	}

	public SNetHashMap(int initCapacity) {
		this(initCapacity, DEF_FACTOR);
	}

	public SNetHashMap(int initCapacity, double factor) {
		this.factor = factor < 0.1 ? 0.1 : (factor < 256 ? factor : 256);
		this.initCapacity = Math.max(initCapacity, 4);
		setKeyEqualFunc(KeyEqualFunc.DEF_EQUAL);
		reset();
	}

	public SNetHashMap<K, V> setKeyEqualFunc(KeyEqualFunc<K> keyEqualFunc) {
		this.keyEqualFunc = keyEqualFunc == null ? KeyEqualFunc.DEF_EQUAL : keyEqualFunc;
		return this;
	}

	@Override
	public EntryPlus<K, V> getEntity(Object key, boolean absentCreate) {
		final KeyEqualFunc<K> keyEqualFunc = this.keyEqualFunc;
		final HashNode<K, V>[] tables = this.tables;
		final int hash = MapPlus.hash(key);
		final int idx = hash & (tables.length - 1);
		HashNode<K, V> node = tables[idx];
		for (; node != null; node = node.next) {
			if (node.hash == hash && keyEqualFunc.equals(node.key, key))
				return node;
		}
		if (absentCreate) {
			node = createNode(hash, (K) key);
			if (addNode(tables, idx, node) > threshold)
				rehashTable();
		}
		return node;
	}

	protected void rehashTable() {
		final HashNode<K, V>[] tables = this.tables;
		final int newCapacity = tables.length << 1;
		final HashNode<K, V>[] newTables = new HashNode[newCapacity];
		final int newMask = newCapacity - 1;
		HashNode<K, V> start, end;
		for (int i = 0, len = tables.length, idx, prevIdx = -1; i < len; ++i, prevIdx = -1) {
			HashNode<K, V> node = tables[i];
			while ((start = end = node) != null) {
				idx = prevIdx == -1 ? (node.hash & newMask) : prevIdx;
				while ((node = node.next) != null && (prevIdx = (node.hash & newMask)) == idx)
					end = node;
				end.next = newTables[idx];
				newTables[idx] = start;
			}
			tables[i] = null;
		}
		this.tables = newTables;
		this.threshold = getThreshold(newCapacity);
	}

	@Override
	public EntryPlus<K, V> removeEntity(Object key) {
		return removeEntity(key, null, false);
	}

	@Override
	public EntryPlus<K, V> removeEntity(Object key, Object value) {
		return removeEntity(key, value, true);
	}

	protected EntryPlus<K, V> removeEntity(Object key, Object value, boolean equalValue) {
		final KeyEqualFunc<K> equalFunc = this.keyEqualFunc;
		final HashNode<K, V>[] tables = this.tables;
		final int hash = MapPlus.hash(key);
		final int idx = hash & (tables.length - 1);
		for (HashNode<K, V> node = tables[idx], prev = null; node != null; prev = node, node = node.next) {
			if (node.hash == hash && equalFunc.equals(node.key, key)) {
				if (equalValue && !Objects.equals(node.value, value))
					return null;
				removeNode(tables, idx, prev, node);
				return node;
			}
		}
		return null;
	}


	protected int addNode(HashNode<K, V>[] tables, int idx, HashNode<K, V> node) {
		node.next = tables[idx];
		tables[idx] = node;
		return ++size;
	}

	protected void removeNode(HashNode<K, V>[] tables, int idx, HashNode<K, V> prev, HashNode<K, V> node) {
		if (prev == null)
			tables[idx] = node.next;
		else
			prev.next = node.next;
		--size;
	}

	protected HashNode<K, V> createNode(int hash, K key) {
		return new HashNode<>(hash, key);
	}

	protected int getThreshold(int capacity) {
		if (capacity < MAX_TABLE_CAPACITY) {
			int i = (int) (capacity * factor);
			return i < 0 ? Integer.MAX_VALUE : i;
		}
		return Integer.MAX_VALUE;
	}

	@Override
	public int size() {
		return size;
	}


	@Override
	public boolean containsValue(Object value) {
		if (size > 0) {
			for (HashNode<K, V> node : this.tables) {
				for (; node != null; node = node.next)
					if (Objects.equals(node.value, value))
						return true;
			}
		}
		return false;
	}

	@Override
	public void clear() {
		if (size > 0) {
			HashNode<K, V>[] tables = this.tables;
			for (int i = 0, len = tables.length; i < len; ++i)
				tables[i] = null;
			this.size = 0;
		}
	}

	public void reset() {
		int capacity = MathUtil.ceil2((int) (initCapacity / factor));
		this.size = 0;
		this.tables = new HashNode[capacity];
		this.threshold = getThreshold(capacity);
	}

	@Override
	public Iterator<EntryPlus<K, V>> iterator() {
		return new It<>(this, v -> v);
	}

	@Override
	public Set<K> keySet() {
		return new KeySet<>(this, v -> (K) v.key, Map::containsKey);
	}

	@Override
	public Collection<V> values() {
		return new Coll<>(this, v -> (V) v.value, Map::containsValue);
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return new Coll<>(this, v -> v, (a, b) -> false);
	}

	protected static class It<E> implements Iterator<E> {
		protected final SNetHashMap map;
		protected final Function<HashNode, E> getter;
		protected int idx;
		protected HashNode prev;
		protected HashNode node;

		public It(SNetHashMap map, Function<HashNode, E> getter) {
			this.map = map;
			this.getter = getter;
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
			return getter.apply(prev);
		}

		protected void nextNode() {
			HashNode[] tables = map.tables;
			do {
				if (node != null)
					node = node.next;
				else if (idx < tables.length)
					node = tables[idx++];
				else
					break;
			} while (node == null);
		}

		@Override
		public void remove() {
			map.remove(prev.key);
		}
	}

	protected static class Coll<E> implements Set<E> {
		protected final SNetHashMap map;
		protected final Function<HashNode, E> getter;
		protected final BiPredicate<SNetHashMap, Object> containFunc;

		public Coll(SNetHashMap map, Function<HashNode, E> getter, BiPredicate<SNetHashMap, Object> containFunc) {
			this.map = map;
			this.getter = getter;
			this.containFunc = containFunc;
		}

		@Override
		public int size() {
			return map.size();
		}

		@Override
		public boolean isEmpty() {
			return map.isEmpty();
		}


		@Override
		public Object[] toArray() {
			Object[] array = new Object[size()];
			return toArray(array);
		}

		@Override
		public <T> T[] toArray(T[] a) {
			if (a.length < size())
				a = (T[]) Array.newInstance(a.getClass().getComponentType(), size());
			int i = 0;
			for (E e : this)
				a[i] = (T) e;
			return a;
		}

		@Override
		public Iterator<E> iterator() {
			return new It<>(map, getter);
		}

		@Override
		public boolean add(E e) {
			return false;
		}

		@Override
		public boolean remove(Object o) {
			return false;
		}

		@Override
		public boolean addAll(Collection<? extends E> c) {
			return false;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			return false;
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			return false;
		}

		@Override
		public boolean contains(Object o) {
			return containFunc.test(map, o);
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			SNetHashMap map = this.map;
			BiPredicate<SNetHashMap, Object> containFunc = this.containFunc;
			for (Object e : c) {
				if (!containFunc.test(map, e))
					return false;
			}
			return true;
		}

		@Override
		public void clear() {
			map.clear();
		}
	}

	protected static class KeySet<E> extends Coll<E> {

		public KeySet(SNetHashMap map, Function<HashNode, E> getter, BiPredicate<SNetHashMap, Object> containFunc) {
			super(map, getter, containFunc);
		}

		@Override
		public boolean remove(Object o) {
			return map.removeEntity(o) != null;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			SNetHashMap map = this.map;
			boolean b = false;
			for (Object e : c) {
				if (map.removeEntity(e) != null)
					b = true;
			}
			return b;
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			boolean b = false;
			for (Iterator<HashNode> it = map.iterator(); it.hasNext(); ) {
				HashNode node = it.next();
				if (!c.contains(node.key)) {
					it.remove();
					b = true;
				}
			}
			return b;
		}
	}


}
