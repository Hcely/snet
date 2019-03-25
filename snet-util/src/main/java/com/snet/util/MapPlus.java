package com.snet.util;

import java.util.Map;

public interface MapPlus<K, V> extends Map<K, V>, Iterable<EntryPlus<K, V>> {
	static int hash(int hashCode) {
		return hashCode ^ (hashCode >>> 16);
	}

	default EntryPlus<K, V> getEntity(Object key) {
		return getEntity(key, false);
	}

	EntryPlus<K, V> getEntity(Object key, boolean absentCreate);

	EntryPlus<K, V> removeEntity(Object key);

	EntryPlus<K, V> removeEntity(Object key, Object value);

	@Override
	default boolean containsKey(Object key) {
		return getEntity(key) != null;
	}


	@Override
	default V get(Object key) {
		EntryPlus<K, V> node = getEntity(key);
		return node == null ? null : node.getValue();
	}

	@Override
	default V getOrDefault(Object key, V defaultValue) {
		EntryPlus<K, V> node = getEntity(key);
		return node == null ? defaultValue : node.getValue();
	}

	@Override
	default V put(K key, V value) {
		EntryPlus<K, V> node = getEntity(key, true);
		return node.setValue(value);
	}

	@Override
	default V putIfAbsent(K key, V value) {
		EntryPlus<K, V> node = getEntity(key);
		return node == null ? put(key, value) : node.getValue();
	}


	@Override
	default boolean replace(K key, V oldValue, V newValue) {
		EntryPlus<K, V> node = getEntity(key);
		return node != null && node.compareAndSetValue(oldValue, newValue);
	}

	@Override
	default V replace(K key, V value) {
		EntryPlus<K, V> node = getEntity(key);
		return node == null ? null : node.setValue(value);
	}

	@Override
	default V remove(Object key) {
		EntryPlus<K, V> node = removeEntity(key);
		return node == null ? null : node.getValue();
	}

	@Override
	default boolean remove(Object key, Object value) {
		return removeEntity(key, value) != null;
	}

	@Override
	default void putAll(Map<? extends K, ? extends V> m) {
		for (Entry<? extends K, ? extends V> e : m.entrySet())
			put(e.getKey(), e.getValue());
	}

	@Override
	default boolean isEmpty() {
		return size() == 0;
	}
}
