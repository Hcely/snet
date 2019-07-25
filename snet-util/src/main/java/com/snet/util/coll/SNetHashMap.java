package com.snet.util.coll;

import com.snet.IBuilder;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.ToIntFunction;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SNetHashMap<K, V> extends AbsHashMap<V> implements MapPlus<K, V> {
	public static final double DEF_FACTOR = 4;
	public static final int DEF_INIT_CAPACITY = 16;
	public static final int MAX_TABLE_CAPACITY = 1 << 24;

	public static <K, V> Builder<K, V> builder() {
		return new Builder<>();
	}

	public static class Builder<K, V> implements IBuilder<SNetHashMap<K, V>> {
		protected int initCapacity = DEF_INIT_CAPACITY;
		protected double factor = DEF_FACTOR;
		protected ToIntFunction<Object> hashFunc;
		protected KeyEqualFunc<K> keyEqualFunc;

		protected Builder() {
		}

		public int getInitCapacity() {
			return initCapacity;
		}

		public Builder<K, V> setInitCapacity(int initCapacity) {
			this.initCapacity = initCapacity;
			return this;
		}

		public double getFactor() {
			return factor;
		}

		public Builder<K, V> setFactor(double factor) {
			this.factor = factor;
			return this;
		}

		public ToIntFunction<Object> getHashFunc() {
			return hashFunc;
		}

		public Builder<K, V> setHashFunc(ToIntFunction<Object> hashFunc) {
			this.hashFunc = hashFunc;
			return this;
		}

		public KeyEqualFunc<K> getKeyEqualFunc() {
			return keyEqualFunc;
		}

		public Builder<K, V> setKeyEqualFunc(KeyEqualFunc<K> keyEqualFunc) {
			this.keyEqualFunc = keyEqualFunc;
			return this;
		}

		@Override
		public SNetHashMap<K, V> build() {
			return new SNetHashMap<>(initCapacity, factor, hashFunc, keyEqualFunc);
		}
	}


	protected static class KeyNodeEntry<K, V> extends NodeEntry<V> implements EntryPlus<K, V> {
		protected final K key;
		protected KeyNodeEntry<K, V> next;

		public KeyNodeEntry(int hash, K key) {
			super(hash);
			this.key = key;
		}

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public KeyNodeEntry<K, V> getNext() {
			return next;
		}

		@Override
		public void setNext(NodeEntry<V> next) {
			this.next = (KeyNodeEntry<K, V>) next;
		}

		@Override
		public String toString() {
			return key + "=" + value;
		}
	}

	protected final ToIntFunction<Object> hashFunc;
	protected final KeyEqualFunc<K> keyEqualFunc;
	protected KeyNodeEntry<K, V>[] tables;

	public SNetHashMap() {
		this(DEF_INIT_CAPACITY);
	}

	public SNetHashMap(int initCapacity) {
		this(initCapacity, DEF_FACTOR);
	}

	public SNetHashMap(int initCapacity, double factor) {
		this(initCapacity, factor, null, null);
	}

	public SNetHashMap(int initCapacity, double factor, ToIntFunction<Object> hashFunc, KeyEqualFunc<K> keyEqualFunc) {
		super(initCapacity, factor);
		this.hashFunc = hashFunc == null ? Object::hashCode : hashFunc;
		this.keyEqualFunc = keyEqualFunc == null ? KeyEqualFunc.DEF_EQUAL : keyEqualFunc;
		reset();
	}

	@Override
	protected NodeEntry<V>[] getTables() {
		return tables;
	}

	@Override
	protected NodeEntry<V>[] newTables(int capacity) {
		return new KeyNodeEntry[capacity];
	}

	@Override
	protected void setTables(NodeEntry<V>[] tables) {
		this.tables = (KeyNodeEntry<K, V>[]) tables;
	}


	@Override
	public EntryPlus<K, V> getEntity(Object key, boolean absentCreate) {
		final KeyEqualFunc<K> keyEqualFunc = this.keyEqualFunc;
		final KeyNodeEntry<K, V>[] tables = this.tables;
		final int hash = MapPlus.hash(key == null ? 0 : hashFunc.applyAsInt(key));
		final int idx = hash & (tables.length - 1);
		KeyNodeEntry<K, V> node = tables[idx];
		for (; node != null; node = node.next) {
			if (node.hash == hash && keyEqualFunc.equals(node.key, key))
				return node;
		}
		if (absentCreate) {
			node = createNode(hash, (K) key);
			addNode(tables, idx, node);
		}
		return node;
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
		final KeyEqualFunc<K> keyEqualFunc = this.keyEqualFunc;
		final KeyNodeEntry<K, V>[] tables = this.tables;
		final int hash = MapPlus.hash(key == null ? 0 : hashFunc.applyAsInt(key));
		final int idx = hash & (tables.length - 1);
		for (KeyNodeEntry<K, V> node = tables[idx], prev = null; node != null; prev = node, node = node.next) {
			if (node.hash == hash && keyEqualFunc.equals(node.key, key)) {
				if (equalValue && !Objects.equals(node.value, value))
					return null;
				removeNode(tables, idx, prev, node);
				return node;
			}
		}
		return null;
	}

	protected KeyNodeEntry<K, V> createNode(int hash, K key) {
		return new KeyNodeEntry<>(hash, key);
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

	protected static class It<E> extends AbsIt<E, KeyNodeEntry> {
		protected final Function<KeyNodeEntry, E> getter;

		public It(SNetHashMap map, Function<KeyNodeEntry, E> getter) {
			super(map);
			this.getter = getter;
		}


		@Override
		protected E get(KeyNodeEntry node) {
			return getter.apply(node);
		}

		@Override
		protected void removeNode(KeyNodeEntry node) {
			((SNetHashMap) map).remove(node.key);
		}
	}

	protected static class Coll<E> implements Set<E> {
		protected final SNetHashMap map;
		protected final Function<KeyNodeEntry, E> getter;
		protected final BiPredicate<SNetHashMap, Object> containFunc;

		public Coll(SNetHashMap map, Function<KeyNodeEntry, E> getter, BiPredicate<SNetHashMap, Object> containFunc) {
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

		public KeySet(SNetHashMap map, Function<KeyNodeEntry, E> getter, BiPredicate<SNetHashMap, Object> containFunc) {
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
			for (Iterator<KeyNodeEntry> it = map.iterator(); it.hasNext(); ) {
				KeyNodeEntry node = it.next();
				if (!c.contains(node.key)) {
					it.remove();
					b = true;
				}
			}
			return b;
		}
	}


}
