package com.snet.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;

@SuppressWarnings({"rawtypes", "unchecked"})
public class BPTreeMap<K, V> implements Map<K, V>, Iterable<Map.Entry<K, V>> {

	protected static class BPTNode<K> {
		protected K key;
		protected BlockNode<K> parent;
		protected BPTNode<K> prev, next;

		public BPTNode(K key) {
			this.key = key;
		}

		public boolean remove() {
			BlockNode<K> parent = this.parent;
			if (parent == null)
				return false;
			parent.removeNode(this);
			return true;
		}
	}

	public static final class LeafNode<K, V> extends BPTNode<K> implements Entry<K, V> {
		protected V value;

		LeafNode(K key, V value) {
			super(key);
			this.value = value;
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

		public V setValueIfAbsent(V value) {
			V tmp = this.value;
			if (tmp == null)
				this.value = value;
			return tmp;
		}

		public boolean compareAndSetValue(V oldValue, V newValue) {
			V tmp = value;
			if (Objects.equals(tmp, oldValue)) {
				value = newValue;
				return true;
			}
			return false;
		}


		public LeafNode<K, V> getPrev() {
			return (LeafNode<K, V>) prev;
		}

		public LeafNode<K, V> getNext() {
			return (LeafNode<K, V>) next;
		}

		@Override
		public String toString() {
			return key + "=" + value;
		}
	}


	protected static class BlockNode<K> extends BPTNode<K> {
		protected final boolean basicBlock;
		protected final BPTreeMap map;
		protected int size;
		protected BPTNode<K> head, tail;

		public BlockNode(BPTreeMap map) {
			super(null);
			this.basicBlock = true;
			this.map = map;
			this.size = 0;
		}

		public BlockNode(boolean basicBlock, BPTreeMap<K, Object> map, BPTNode<K> head, BPTNode<K> tail, int size) {
			super(head.key);
			this.basicBlock = basicBlock;
			this.map = map;
			this.head = head;
			this.tail = tail;
			this.size = size;
			for (int i = 0; i < size; ++i, head = head.next)
				head.parent = this;
		}

		protected void setHead(BPTNode<K> head) {
			this.head = head;
			this.key = head == null ? null : head.key;
			BlockNode<K> p = parent, n = this;
			while (p != null && p.head == n) {
				p.key = n.key;
				n = p;
				p = p.parent;
			}
			if (prev == null && basicBlock)
				map.head = (LeafNode) head;
		}

		protected void setTail(BPTNode<K> tail) {
			this.tail = tail;
			if (next == null && basicBlock)
				map.tail = (LeafNode) tail;
		}

		protected void sub() {
			BPTNode<K> node = head;
			final int newSize = size >>> 1;
			final int subSize = size - newSize;
			for (int i = 0; i < newSize; ++i)
				node = node.next;
			this.tail = node.prev;
			this.size = newSize;
			BlockNode<K> sub = new BlockNode<>(basicBlock, map, node, tail, subSize);
			this.parent.addNode(this, sub);
		}

		protected void combineNext(BlockNode<K> nextNode) {
			for (BPTNode<K> n = nextNode.head; n != null && n.parent == nextNode; n = n.next)
				n.parent = this;
			size += nextNode.size;
			tail = nextNode.tail;

			nextNode.remove();
		}

		protected void addNode(BPTNode<K> prev, BPTNode<K> node) {
			node.parent = this;
			node.prev = prev;
			BPTNode<K> next;
			if (prev == null) {
				next = node.next = head;
				setHead(node);
			} else {
				next = node.next = prev.next;
				prev.next = node;
			}
			if (next != null)
				next.prev = node;

			if (tail == prev)
				setTail(node);
			if (basicBlock)
				++map.size;
			if (++size > map.maxSize) {
				if (parent == null)
					map.setRoot(new BlockNode<>(false, map, this, this, 1));
				sub();
			}
		}

		protected void removeNode(BPTNode<K> node) {
			node.parent = null;
			BPTNode<K> cPrev = node.prev, cNext = node.next;
			if (cPrev != null)
				cPrev.next = cNext;
			if (cNext != null)
				cNext.prev = cPrev;
			if (head == node)
				setHead(cNext);
			if (tail == node)
				setTail(cPrev);
			if (basicBlock)
				--map.size;
			if (--size < map.minSize) {
				if (parent == null) {
					if (size == 1 && head.getClass() == BlockNode.class)
						map.setRoot((BlockNode<K>) head);
				} else if (next == null)
					left();
				else
					right();
			}
		}

		protected void left() {
			BlockNode<K> prev = (BlockNode<K>) this.prev;
			if (prev.size + size < map.threshold) {
				prev.combineNext(this);
			} else {
				BPTNode<K> cNode = prev.tail;
				cNode.parent = this;
				cNode = cNode.prev;
				cNode.parent = this;

				this.size += 2;
				this.setHead(cNode);

				prev.tail = cNode.prev;
				prev.size -= 2;
			}
		}

		protected void right() {
			BlockNode<K> next = (BlockNode<K>) this.next;
			if (next.size + size < map.threshold) {
				this.combineNext(next);
			} else {
				BPTNode<K> cNode = next.head;
				cNode.parent = this;
				cNode = cNode.next;
				cNode.parent = this;

				this.size += 2;
				this.tail = cNode;

				next.size -= 2;
				next.setHead(cNode.next);
			}
		}

		protected BPTNode<K> findNode(Object key) {
			final Comparator<Object> comparator = this.map.comparator;
			BPTNode<K> node = head, prev = null;
			for (int i = 0, size = this.size, hr; i < size; ++i) {
				if ((hr = comparator.compare(key, node.key)) < 0) {
					break;
				} else if (hr == 0 && node.key.equals(key)) {
					while (node.getClass() != LeafNode.class)
						node = (((BlockNode<K>) node)).head;
					prev = node;
					break;
				}
				prev = node;
				node = node.next;
			}
			return prev;
		}
	}

	private static final Comparator<Object> DEF_COMPARATOR = (o1, o2) -> {
		if (o1 instanceof Comparable)
			return ((Comparable) o1).compareTo(o2);
		return Integer.compare(o1.hashCode(), o2.hashCode());
	};
	public static final int DEF_FACTOR = 4;
	final Comparator<Object> comparator;
	final int maxSize, threshold, minSize;
	BlockNode<K> root;
	LeafNode<K, V> head, tail;
	int size;

	public BPTreeMap() {
		this(DEF_FACTOR, DEF_COMPARATOR);
	}

	public BPTreeMap(Comparator<?> comparator) {
		this(DEF_FACTOR, comparator);
	}

	public BPTreeMap(int factor) {
		this(factor, DEF_COMPARATOR);
	}

	public BPTreeMap(int factor, Comparator<?> comparator) {
		factor = factor > 2 ? factor : 2;
		this.comparator = comparator == null ? DEF_COMPARATOR : (Comparator<Object>) comparator;
		this.minSize = factor;
		this.threshold = (factor << 1) + 2;
		this.maxSize = threshold + 2;
		this.reset();
	}

	protected void setRoot(BlockNode<K> root) {
		this.root = root;
		root.parent = null;
	}

	@Override
	public int size() {
		return size;
	}

	public LeafNode<K, V> findNode(Object key) {
		BlockNode<K> parent = root;
		while (true) {
			BPTNode<K> prev = parent.findNode(key);
			if (prev == null || prev.getClass() == LeafNode.class)
				return (LeafNode<K, V>) prev;
			parent = (BlockNode<K>) prev;
		}
	}

	protected LeafNode<K, V> addNode(LeafNode<K, V> prev, K key, V value) {
		LeafNode<K, V> node = new LeafNode<>(key, value);
		BlockNode<K> parent = prev != null ? prev.parent : (head == null ? root : head.parent);
		parent.addNode(prev, node);
		return node;
	}

	public LeafNode<K, V> getEntity(Object key) {
		return getEntity((K) key, false);
	}


	public LeafNode<K, V> getEntity(K key, boolean absentCreate) {
		LeafNode<K, V> node = findNode(key);
		if (node != null && Objects.equals(node.key, key))
			return node;
		return absentCreate ? addNode(node, key, null) : null;
	}

	public LeafNode<K, V> addEntity(LeafNode<K, V> prev, K key, V value) {
		if (prev != null && Objects.equals(prev.key, key)) {
			prev.setValue(value);
			return prev;
		}
		return addNode(prev, key, value);
	}

	public LeafNode<K, V> removeEntity(Object key) {
		LeafNode<K, V> node = getEntity(key);
		if (node != null)
			node.remove();
		return node;
	}


	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public boolean containsKey(Object key) {
		return getEntity(key) != null;
	}

	@Override
	public boolean containsValue(Object value) {
		for (LeafNode<K, V> node = head; node != null; node = node.getNext()) {
			if (Objects.equals(node.value, value))
				return true;
		}
		return false;
	}

	@Override
	public V get(Object key) {
		LeafNode<K, V> node = getEntity(key);
		return node == null ? null : node.value;
	}

	@Override
	public V getOrDefault(Object key, V defaultValue) {
		LeafNode<K, V> node = getEntity(key);
		return node == null ? defaultValue : node.value;
	}

	@Override
	public V put(K key, V value) {
		LeafNode<K, V> node = getEntity(key, true);
		return node.setValue(value);
	}

	@Override
	public V putIfAbsent(K key, V value) {
		LeafNode<K, V> node = findNode(key);
		if (node == null || !Objects.equals(node.key, key))
			node = addNode(node, key, value);
		return node.value;
	}


	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		LeafNode<K, V> node = getEntity(key, false);
		return node != null && node.compareAndSetValue(oldValue, newValue);
	}

	@Override
	public V replace(K key, V value) {
		LeafNode<K, V> node = getEntity(key, false);
		return node == null ? null : node.setValue(value);
	}

	@Override
	public V remove(Object key) {
		LeafNode<K, V> node = getEntity(key);
		if (node == null)
			return null;
		node.remove();
		return node.value;
	}

	@Override
	public boolean remove(Object key, Object value) {
		LeafNode<K, V> node = getEntity(key);
		return node != null && Objects.equals(node.value, value) && node.remove();
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (Entry<? extends K, ? extends V> e : m.entrySet())
			put(e.getKey(), e.getValue());
	}

	@Override
	public void clear() {
		for (LeafNode<K, V> node = head; node != null; node = node.getNext())
			node.value = null;
		reset();
	}

	public void reset() {
		root = new BlockNode<>(this);
		head = tail = null;
		size = 0;
	}

	@Override
	public Set<K> keySet() {
		return new KeySet<>(this);
	}

	@Override
	public Collection<V> values() {
		return new ValueColl<>(this);
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return new EntrySet<>(this);
	}

	@Override
	public Iterator<Entry<K, V>> iterator() {
		return new It<>(this.head, node -> node);
	}

	protected static abstract class AbsColl<K> implements Collection<K> {
		final BPTreeMap map;
		final Function<LeafNode, K> apply;
		final BiPredicate<BPTreeMap, Object> predicate;

		protected AbsColl(BPTreeMap map, Function<LeafNode, K> apply, BiPredicate<BPTreeMap, Object> predicate) {
			this.map = map;
			this.apply = apply;
			this.predicate = predicate;
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
		public Iterator<K> iterator() {
			return new It<>(map.head, apply);
		}

		@Override
		public boolean add(K k) {
			return false;
		}


		@Override
		public boolean addAll(Collection<? extends K> c) {
			return false;
		}

		@Override
		public boolean contains(Object o) {
			return predicate.test(map, o);
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			final BPTreeMap map = this.map;
			final BiPredicate<BPTreeMap, Object> predicate = this.predicate;
			for (Object e : c) {
				if (!predicate.test(map, e))
					return false;
			}
			return true;
		}

		@Override
		public boolean remove(Object o) {
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
		public void clear() {
			map.clear();
		}

		@Override
		public Object[] toArray() {
			return toArray(new Object[map.size()]);
		}

		@Override
		public <T> T[] toArray(T[] a) {
			if (a.length < map.size())
				a = (T[]) Array.newInstance(a.getClass().getComponentType(), map.size());
			int i = 0;
			final BPTreeMap map = this.map;
			final Function<LeafNode, K> apply = this.apply;
			for (LeafNode node = map.head; node != null; node = node.getNext(), ++i)
				a[i] = (T) apply.apply(node);
			return a;
		}
	}

	protected static class It<K> implements Iterator<K> {
		protected LeafNode prev, node;
		protected final Function<LeafNode, K> apply;

		public It(LeafNode node, Function<LeafNode, K> apply) {
			this.node = node;
			this.apply = apply;
		}

		@Override
		public boolean hasNext() {
			return node != null;
		}

		@Override
		public K next() {
			K value = apply.apply(node);
			prev = node;
			node = node.getNext();
			return value;
		}

		@Override
		public void remove() {
			prev.remove();
		}
	}

	protected static class KeySet<K> extends AbsColl<K> implements Set<K> {

		protected KeySet(BPTreeMap map) {
			super(map, node -> (K) node.getKey(), BPTreeMap::containsKey);
		}


		@Override
		public boolean remove(Object o) {
			return map.removeEntity(o) != null;
		}


		@Override
		public boolean retainAll(Collection<?> c) {
			boolean change = false;
			for (LeafNode node = map.head; node != null; node = node.getNext()) {
				if (!c.contains(node.key))
					change = node.remove();
			}
			return change;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			boolean change = false;
			for (Object e : c) {
				if (map.removeEntity(e) != null)
					change = true;
			}
			return change;
		}
	}

	protected static class ValueColl<V> extends AbsColl<V> {
		protected ValueColl(BPTreeMap map) {
			super(map, node -> (V) node.getValue(), BPTreeMap::containsValue);
		}
	}

	protected static final class EntrySet<K, V> extends AbsColl<Entry<K, V>> implements Set<Entry<K, V>> {
		protected EntrySet(BPTreeMap map) {
			super(map, node -> (Entry<K, V>) node, (m, o) -> false);
		}
	}
}
