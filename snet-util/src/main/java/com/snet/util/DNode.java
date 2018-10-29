package com.snet.util;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

@SuppressWarnings("rawtypes")
public class DNode<T> extends Node<T> {
	private static final AtomicReferenceFieldUpdater<DNode, DNode> PREV_UPDATER = AtomicReferenceFieldUpdater
			.newUpdater(DNode.class, DNode.class, "prev");
	private static final AtomicReferenceFieldUpdater<DNode, DNode> NEXT_UPDATER = AtomicReferenceFieldUpdater
			.newUpdater(DNode.class, DNode.class, "next");

	protected volatile DNode<T> prev, next;

	public DNode() {
	}

	public DNode(T data) {
		super(data);
	}

	public boolean casPrev(DNode<T> oldValue, DNode<T> newValue) {
		return PREV_UPDATER.compareAndSet(this, oldValue, newValue);
	}

	public boolean casNext(DNode<T> oldValue, DNode<T> newValue) {
		return NEXT_UPDATER.compareAndSet(this, oldValue, newValue);
	}

	public DNode<T> getPrev() {
		return prev;
	}

	public void setPrev(DNode<T> prev) {
		this.prev = prev;
	}

	public DNode<T> getNext() {
		return next;
	}

	public void setNext(DNode<T> next) {
		this.next = next;
	}

}
