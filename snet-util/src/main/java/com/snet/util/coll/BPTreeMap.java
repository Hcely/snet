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

@SuppressWarnings({"rawtypes", "unchecked"})
public class BPTreeMap<K, V> implements MapPlus<K, V> {

	public static class Builder<K, V> implements IBuilder<BPTreeMap<K, V>> {
		protected int factor = DEF_FACTOR;
		protected KeyComparator<K, ?> comparator;
		protected KeyEqualFunc<K> equalFunc;

		public int getFactor() {
			return factor;
		}

		public Builder<K, V> setFactor(int factor) {
			this.factor = factor;
			return this;
		}

		public KeyComparator<K, ?> getComparator() {
			return comparator;
		}

		public Builder<K, V> setComparator(KeyComparator<K, ?> comparator) {
			this.comparator = comparator;
			return this;
		}

		public KeyEqualFunc<K> getEqualFunc() {
			return equalFunc;
		}

		public Builder<K, V> setEqualFunc(KeyEqualFunc<K> equalFunc) {
			this.equalFunc = equalFunc;
			return this;
		}

		@Override
		public BPTreeMap<K, V> build() {
			return new BPTreeMap<>(factor, comparator, equalFunc);
		}
	}

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

	public static final class LeafNode<K, V> extends BPTNode<K> implements EntryPlus<K, V> {
		protected V value;

		public LeafNode(K key) {
			this(key, null);
		}

		public LeafNode(K key, V value) {
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
			BPTNode<K> cPrev = node.prev, cNext = node.next;
			node.parent = null;
			node.prev = null;
			node.next = null;
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

		protected BPTNode<K> floorEntity(Object key) {
			final KeyComparator<K, Object> comparator = this.map.comparator;
			final KeyEqualFunc<K> equalFunc = this.map.equalFunc;
			BPTNode<K> node = head, prev = null;
			for (int i = 0, size = this.size, hr; i < size; ++i) {
				if ((hr = comparator.compare(node.key, key)) > 0) {
					break;
				} else if (hr == 0 && equalFunc.equals(node.key, key)) {
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

	public static final KeyComparator<Object, Object> DEF_COMPARATOR = (o1, o2) -> {
		if (o1 instanceof Comparable)
			return ((Comparable) o1).compareTo(o2);
		return Integer.compare(o1.hashCode(), o2.hashCode());
	};
	public static final int DEF_FACTOR = 4;
	protected final KeyComparator<K, Object> comparator;
	protected final KeyEqualFunc<K> equalFunc;
	final int maxSize, threshold, minSize;
	BlockNode<K> root;
	LeafNode<K, V> head, tail;
	int size;

	public BPTreeMap() {
		this(DEF_FACTOR, null);
	}

	public BPTreeMap(KeyComparator<K, ?> comparator) {
		this(DEF_FACTOR, comparator);
	}

	public BPTreeMap(KeyComparator<K, ?> comparator, KeyEqualFunc<K> equalFunc) {
		this(DEF_FACTOR, comparator, equalFunc);
	}

	public BPTreeMap(int factor) {
		this(factor, null);
	}

	public BPTreeMap(int factor, KeyComparator<K, ?> comparator) {
		this(factor, comparator, KeyEqualFunc.DEF_EQUAL);
	}

	public BPTreeMap(int factor, KeyComparator<K, ?> comparator, KeyEqualFunc<K> equalFunc) {
		factor = factor > 2 ? factor : 2;
		this.comparator =
				comparator == null ? (KeyComparator<K, Object>) DEF_COMPARATOR : (KeyComparator<K, Object>) comparator;
		this.equalFunc = equalFunc == null ? KeyEqualFunc.DEF_EQUAL : equalFunc;
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

	protected LeafNode<K, V> addNode(LeafNode<K, V> prev, LeafNode<K, V> node) {
		BlockNode<K> parent = prev != null ? prev.parent : (head == null ? root : head.parent);
		parent.addNode(prev, node);
		return node;
	}

	public LeafNode<K, V> firstEntity() {
		return head;
	}

	public LeafNode<K, V> lastEntity() {
		return tail;
	}

	public LeafNode<K, V> floorEntity(Object key) {
		BlockNode<K> parent = root;
		while (true) {
			BPTNode<K> prev = parent.floorEntity(key);
			if (prev == null || prev.getClass() == LeafNode.class)
				return (LeafNode<K, V>) prev;
			parent = (BlockNode<K>) prev;
		}
	}

	public LeafNode<K, V> ceilEntity(Object key) {
		LeafNode<K, V> prev = floorEntity(key);
		if (prev == null)
			return head;
		return comparator.compare(prev.key, key) == 0 ? prev : prev.getNext();
	}

	@Override
	public LeafNode<K, V> getEntity(Object key) {
		return getEntity(key, false);
	}

	@Override
	public LeafNode<K, V> getEntity(Object key, boolean absentCreate) {
		LeafNode<K, V> prev = floorEntity(key);
		if (prev != null && equalFunc.equals(prev.key, key))
			return prev;
		return absentCreate ? addNode(prev, new LeafNode<>((K) key)) : null;
	}

	public LeafNode<K, V> putEntity(LeafNode<K, V> node) {
		LeafNode<K, V> prev = floorEntity(node.key);
		if (prev != null && equalFunc.equals(prev.key, node.key))
			return prev;
		return addNode(prev, node);
	}

	@Override
	public LeafNode<K, V> removeEntity(Object key) {
		LeafNode<K, V> node = getEntity(key);
		if (node != null)
			node.remove();
		return node;
	}

	@Override
	public EntryPlus<K, V> removeEntity(Object key, Object value) {
		LeafNode<K, V> node = getEntity(key);
		if (node != null && Objects.equals(node.getValue(), value) && node.remove())
			return node;
		return null;
	}

	@Override
	public V putIfAbsent(K key, V value) {
		LeafNode<K, V> prev = floorEntity(key);
		if (prev != null && equalFunc.equals(prev.key, key))
			return prev.getValue();
		addNode(prev, new LeafNode<>(key, value));
		return null;
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
	public void clear() {
		LeafNode<K, V> node = head, next;
		reset();
		for (; node != null; node = next) {
			next = node.getNext();
			node.key = null;
			node.value = null;
			node.parent = null;
			node.prev = null;
			node.next = null;
		}
	}

	public void reset() {
		root = new BlockNode<>(this);
		head = tail = null;
		size = 0;
	}

	@Override
	public Set<K> keySet() {
		return new KeySet<>(this, e -> (K) e.key, Map::containsKey);
	}

	@Override
	public Collection<V> values() {
		return new Coll<>(this, e -> (V) e.value, Map::containsValue);
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return new Coll<>(this, e -> e, (a, b) -> false);
	}

	@Override
	public Iterator<EntryPlus<K, V>> iterator() {
		return new It<>(this.head, node -> node);
	}

	public interface KeyComparator<K, E> {
		int compare(K key, E keyObj);
	}

	protected static class Coll<K> implements Set<K> {
		final BPTreeMap map;
		final Function<LeafNode, K> getter;
		final BiPredicate<BPTreeMap, Object> containFunc;

		protected Coll(BPTreeMap map, Function<LeafNode, K> getter, BiPredicate<BPTreeMap, Object> containFunc) {
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
		public Iterator<K> iterator() {
			return new It<>(map.head, getter);
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
			return containFunc.test(map, o);
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			final BPTreeMap map = this.map;
			final BiPredicate<BPTreeMap, Object> containFunc = this.containFunc;
			for (Object e : c) {
				if (!containFunc.test(map, e))
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
			final Function<LeafNode, K> getter = this.getter;
			for (LeafNode node = map.head; node != null; node = node.getNext(), ++i)
				a[i] = (T) getter.apply(node);
			return a;
		}
	}

	protected static class It<K> implements Iterator<K> {
		protected LeafNode prev, node;
		protected final Function<LeafNode, K> getter;

		public It(LeafNode node, Function<LeafNode, K> getter) {
			this.node = node;
			this.getter = getter;
		}

		@Override
		public boolean hasNext() {
			return node != null;
		}

		@Override
		public K next() {
			K value = getter.apply(node);
			prev = node;
			node = node.getNext();
			return value;
		}

		@Override
		public void remove() {
			prev.remove();
		}
	}

	protected static class KeySet<K> extends Coll<K> {
		protected KeySet(BPTreeMap map, Function<LeafNode, K> getter, BiPredicate<BPTreeMap, Object> predicate) {
			super(map, getter, predicate);
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

}
