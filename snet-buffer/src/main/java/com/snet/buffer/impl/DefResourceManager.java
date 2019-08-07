package com.snet.buffer.impl;

import com.snet.buffer.SNetResource;
import com.snet.buffer.SNetResourceFactory;
import com.snet.buffer.SNetResourceManager;
import com.snet.util.coll.KeyEqualFunc;
import com.snet.util.coll.SNetHashMap;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

@SuppressWarnings("unchecked")
public class DefResourceManager implements SNetResourceManager {
	public static final long DEF_EXPIRE_IN = 2000;
	protected final Map<SNetResource, ResourceState> resources;
	protected final Deque<ResourceState> freeResourceStates;
	protected long sumCapacity = 0;
	protected ResourceFactoryNode factories;
	protected long expireIn = DEF_EXPIRE_IN;

	public DefResourceManager() {
		this.resources = SNetHashMap.builder().setKeyEqualFunc(KeyEqualFunc.IDENTITY_EQUAL).build();
		this.freeResourceStates = new ConcurrentLinkedDeque<>();
	}

	public long getExpireIn() {
		return expireIn;
	}

	public void setExpireIn(long expireIn) {
		this.expireIn = expireIn;
	}

	@Override
	public long getSumCapacity() {
		return sumCapacity;
	}

	@Override
	public SNetResource allocate(long capacity) {
		for (Iterator<ResourceState> it = freeResourceStates.iterator(); it.hasNext(); ) {
			ResourceState state = it.next();
			if (state.state == ResourceState.FREE_STATE) {
				if (state.resource.getCapacity() == capacity && state
						.casState(ResourceState.FREE_STATE, ResourceState.BUSY_STATE)) {
					state.lastTime = System.currentTimeMillis();
					it.remove();
					return state.resource;
				}
			} else {
				it.remove();
			}
		}
		return creteResource(capacity);
	}

	protected synchronized SNetResource creteResource(long capacity) {
		ResourceFactoryNode factories = this.factories;
		SNetResource resource = factories == null ? null : factories.create(this, capacity);
		if (resource == null) {
			return null;
		}
		resource.initialize();
		ResourceState state = new ResourceState(resource);
		resources.put(resource, state);
		sumCapacity += resource.getCapacity();
		state.state = ResourceState.BUSY_STATE;
		return resource;
	}

	protected synchronized void removeState(SNetResource resource) {
		ResourceState state = resources.remove(resource);
		if (state != null) {
			sumCapacity -= resource.getCapacity();
			state.state = ResourceState.RECYCLED_STATE;
		}
	}

	protected synchronized ResourceState getState(SNetResource resource) {
		return resources.get(resource);
	}

	@Override
	public void recycle(SNetResource resource) {
		ResourceState state = getState(resource);
		if (state != null && state.casState(ResourceState.BUSY_STATE, ResourceState.FREE_STATE)) {
			state.lastTime = System.currentTimeMillis();
			this.freeResourceStates.add(state);
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

	@Override
	public int recycleResources() {
		final List<ResourceState> recycleResources = new LinkedList<>();
		final long expireTime = System.currentTimeMillis() - expireIn;
		for (Iterator<ResourceState> it = freeResourceStates.iterator(); it.hasNext(); ) {
			ResourceState state = it.next();
			if (state.getLastTime() < expireTime && state.state == ResourceState.FREE_STATE && state
					.casState(ResourceState.FREE_STATE, ResourceState.RECYCLING_STATE)) {
				it.remove();
				recycleResources.add(state);
			}
		}
		for (ResourceState state : recycleResources) {
			SNetResource resource = state.resource;
			resource.destroy();
			removeState(resource);
		}
		return recycleResources.size();
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

	protected static class ResourceState {
		public static final int FREE_STATE = 0;
		public static final int BUSY_STATE = 1;
		public static final int RECYCLING_STATE = 2;
		public static final int RECYCLED_STATE = 3;

		protected static final AtomicIntegerFieldUpdater<ResourceState> STATE_UPDATER = AtomicIntegerFieldUpdater
				.newUpdater(ResourceState.class, "state");
		protected final SNetResource resource;
		protected final long createTime;
		protected long lastTime;
		protected volatile int state;

		public ResourceState(SNetResource resource) {
			this.resource = resource;
			this.createTime = System.currentTimeMillis();
			this.lastTime = this.createTime;
			this.state = FREE_STATE;
		}

		public SNetResource getResource() {
			return resource;
		}

		public long getCreateTime() {
			return createTime;
		}

		public long getLastTime() {
			return lastTime;
		}

		public void setLastTime(long lastTime) {
			this.lastTime = lastTime;
		}

		public boolean casState(int oldValue, int newValue) {
			return STATE_UPDATER.compareAndSet(this, oldValue, newValue);
		}
	}
}
