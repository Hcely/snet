package com.snet.util;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class SNode<T> extends Node<T> {
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<SNode, SNode> NEXT_UPDATER = AtomicReferenceFieldUpdater
			.newUpdater(SNode.class, SNode.class, "next");

	protected volatile SNode<T> next;

	public SNode() {
	}

	public SNode(T data) {
		super(data);
	}

	public boolean casNext(SNode<T> oldValue, SNode<T> newValue) {
		return NEXT_UPDATER.compareAndSet(this, oldValue, newValue);
	}

	public SNode<T> getNext() {
		return next;
	}

	public void setNext(SNode<T> next) {
		this.next = next;
	}
}
