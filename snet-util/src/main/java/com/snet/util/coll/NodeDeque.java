package com.snet.util.coll;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class NodeDeque<E> implements List<E>, Deque<E> {
	public static final class Node<E> implements EntryValue<E> {
		protected NodeDeque<E> deque;
		protected Node<E> next, prev;
		protected E value;

		public Node(E value) {
			this.value = value;
		}

		@Override
		public E getValue() {
			return value;
		}

		@Override
		public E setValue(E value) {
			E tmp = this.value;
			this.value = value;
			return tmp;
		}

		public boolean remove() {
			return deque != null && deque.removeNode(this);
		}

		public Node<E> getNext() {
			return next;
		}

		public Node<E> getPrev() {
			return prev;
		}
	}

	protected Node<E> head, tail;
	protected int size;
	protected KeyEqualFunc<E> equalFunc;

	public void addNode(Node<E> node, boolean first) {
		if (head == null) {
			head = tail = node;
		} else if (first) {
			node.next = head;
			head.prev = node;
			head = node;
		} else {
			tail.next = node;
			node.prev = tail;
			tail = node;
		}
		node.deque = this;
		++size;
	}

	public void addNode(Node<E> prev, Node<E> node) {
		if (prev == null)
			addNode(node, true);
		else {
			node.prev = prev;
			node.next = prev.next;
			prev.next = node;
			if (node.next == null)
				tail = node;
			else
				node.next.prev = node;
			node.deque = this;
			++size;
		}
	}


	protected boolean removeNode(Node<E> node) {
		if (node.deque != this)
			return false;
		node.deque = null;
		Node<E> prev = node.prev, next = node.next;
		if (prev == null)
			head.next = next;
		else
			prev.next = next;
		if (next == null)
			tail.prev = prev;
		else
			next.prev = prev;
		--size;
		return true;
	}

	public Node<E> getFirstNode() {
		return head;
	}

	public Node<E> getLastNode() {
		return tail;
	}

	public Node<E> pollFirstNode() {
		Node<E> node = head;
		if (node != null)
			removeNode(node);
		return node;
	}

	public Node<E> pollLastNode() {
		Node<E> node = tail;
		if (node != null)
			removeNode(node);
		return node;
	}

	@Override
	public void addFirst(E e) {
		offerFirst(e);
	}

	@Override
	public void addLast(E e) {
		offerLast(e);
	}

	@Override
	public boolean offerFirst(E e) {
		addNode(new Node<>(e), true);
		return true;
	}

	@Override
	public boolean offerLast(E e) {
		addNode(new Node<>(e), false);
		return true;
	}


	@Override
	public E removeFirst() {
		Node<E> node = pollFirstNode();
		if (node == null)
			throw new NoSuchElementException();
		return node.value;
	}

	@Override
	public E removeLast() {
		Node<E> node = pollLastNode();
		if (node == null)
			throw new NoSuchElementException();
		return node.value;
	}

	@Override
	public E pollFirst() {
		Node<E> node = pollFirstNode();
		return node == null ? null : node.value;
	}

	@Override
	public E pollLast() {
		Node<E> node = pollLastNode();
		return node == null ? null : node.value;
	}

	@Override
	public E getFirst() {
		Node<E> node = getFirstNode();
		if (node == null)
			throw new NoSuchElementException();
		return node.value;

	}

	@Override
	public E getLast() {
		Node<E> node = getLastNode();
		if (node == null)
			throw new NoSuchElementException();
		return node.value;
	}

	@Override
	public E peekFirst() {
		Node<E> node = getFirstNode();
		return node == null ? null : node.value;
	}

	@Override
	public E peekLast() {
		Node<E> node = getLastNode();
		return node == null ? null : node.value;
	}

	@Override
	public boolean removeFirstOccurrence(Object o) {
		Node<E> node = getFirstNode();
		return node != null && equalFunc.equals(node.value, o) && node.remove();
	}

	@Override
	public boolean removeLastOccurrence(Object o) {
		Node<E> node = getLastNode();
		return node != null && equalFunc.equals(node.value, o) && node.remove();
	}

	@Override
	public boolean add(E e) {
		return offerLast(e);
	}

	@Override
	public boolean offer(E e) {
		return offerLast(e);
	}

	@Override
	public E remove() {
		return removeLast();
	}

	@Override
	public E poll() {
		return pollFirst();
	}

	@Override
	public E element() {
		return peek();
	}

	@Override
	public E peek() {
		return peekFirst();
	}

	@Override
	public void push(E e) {
		if (e == null)
			throw new NullPointerException();
		addLast(e);
	}

	@Override
	public E pop() {
		return pollFirst();
	}

	@Override
	public boolean remove(Object o) {
		KeyEqualFunc<E> equalFunc = this.equalFunc;
		for (Node<E> n = head; n != null; n = n.next) {
			if (equalFunc.equals(n.value, o))
				return n.remove();
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object e : c) {
			if (!contains(e))
				return false;
		}
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		for (E e : c)
			add(e);
		return true;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean b = false;
		for (Object e : c) {
			if (remove(e))
				b = true;
		}
		return b;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		if (c == null || c.isEmpty())
			return false;
		boolean b = false;
		for (Node<E> n = head; n != null; n = n.next) {
			if (!c.contains(n.value)) {
				n.remove();
				b = true;
			}
		}
		return b;
	}

	@Override
	public void clear() {
		head = tail = null;
		size = 0;
	}

	@Override
	public E get(int index) {
		return null;
	}

	@Override
	public E set(int index, E element) {
		return null;
	}

	@Override
	public void add(int index, E element) {

	}

	@Override
	public E remove(int index) {
		return null;
	}

	@Override
	public int indexOf(Object o) {
		return 0;
	}

	@Override
	public int lastIndexOf(Object o) {
		return 0;
	}

	@Override
	public ListIterator<E> listIterator() {
		return null;
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return null;
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return null;
	}

	@Override
	public boolean contains(Object o) {
		KeyEqualFunc<E> equalFunc = this.equalFunc;
		for (Node<E> n = head; n != null; n = n.next) {
			if (equalFunc.equals(n.value, o))
				return true;
		}
		return false;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		return head == null;
	}

	@Override
	public Iterator<E> iterator() {
		return new It<>(Node::getNext, head);
	}

	@Override
	public Iterator<E> descendingIterator() {
		return new It<>(Node::getPrev, tail);
	}

	@Override
	public Object[] toArray() {
		return toArray(new Object[size]);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {
		if (a.length < size)
			a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
		int i = 0;
		for (Node<E> n = head; n != null; n = n.next, ++i)
			a[i] = (T) n.value;
		return a;
	}


	protected static class It<E> implements Iterator<E> {
		protected final Function<Node<E>, Node<E>> nextFunc;
		protected Node<E> prev, node;

		public It(Function<Node<E>, Node<E>> nextFunc, Node<E> node) {
			this.nextFunc = nextFunc;
			this.node = node;
		}

		@Override
		public boolean hasNext() {
			return node != null;
		}

		@Override
		public E next() {
			prev = node;
			node = nextFunc.apply(node);
			return prev.value;
		}

		@Override
		public void remove() {
			prev.remove();
		}
	}

}
