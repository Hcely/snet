package com.snet.buffer.impl;

import com.snet.buffer.SNetResource;
import com.snet.buffer.SNetResourceFactory;
import com.snet.buffer.SNetResourceManager;

import java.util.concurrent.atomic.AtomicLong;

public class DefResourceManager implements SNetResourceManager {
	protected AtomicLong sumCapacity = new AtomicLong(0);
	protected ResourceFactoryNode factories;

	public DefResourceManager() {
	}

	@Override
	public long getSumCapacity() {
		return sumCapacity.get();
	}

	@Override
	public SNetResource allocate(long capacity) {
		ResourceFactoryNode factories = this.factories;
		SNetResource resource = factories == null ? null : factories.create(this, capacity);
		if (resource == null) {
			return null;
		}
		resource.initialize();
		sumCapacity.addAndGet(resource.getCapacity());
		return resource;
	}

	@Override
	public void recycle(SNetResource resource) {
		if (!resource.isDestroyed() && resource.getManager() == this) {
			resource.destroy();
			this.sumCapacity.addAndGet(-resource.getCapacity());
		}
	}


	public void addResourceFactory(long maxCapacity, SNetResourceFactory factory) {
		ResourceFactoryNode newNode = new ResourceFactoryNode(maxCapacity, factory);
		ResourceFactoryNode prev = null;
		for (ResourceFactoryNode node = factories; node != null; prev = node, node = node.next) {
			if (node.maxCapacity > maxCapacity) {
				break;
			}
		}
		if (prev == null) {
			newNode.add(factories);
			factories = newNode;
		} else {
			prev.add(newNode);
		}
	}


	protected static class ResourceFactoryNode implements SNetResourceFactory {
		protected final long maxCapacity;
		protected final SNetResourceFactory factory;
		protected ResourceFactoryNode next;

		public ResourceFactoryNode(long maxCapacity, SNetResourceFactory factory) {
			this.maxCapacity = maxCapacity;
			this.factory = factory;
		}

		@Override
		public SNetResource create(SNetResourceManager manager, long capacity) {
			if (capacity > this.maxCapacity) {
				return next == null ? null : next.create(manager, capacity);
			}
			return factory.create(manager, capacity);
		}

		public void add(ResourceFactoryNode node) {
			if (node != null) {
				node.next = this.next;
			}
			this.next = node;
		}
	}

}
