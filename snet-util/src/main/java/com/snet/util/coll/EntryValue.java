package com.snet.util.coll;

import java.util.Objects;

public interface EntryValue<V> {
	V getValue();

	V setValue(V value);

	default V setValueIfNull(V value) {
		V tmp = getValue();
		if (tmp == null)
			setValue(value);
		return tmp;
	}

	default boolean compareAndSetValue(V oldValue, V newValue) {
		V tmp = getValue();
		if (Objects.equals(tmp, oldValue)) {
			setValue(newValue);
			return true;
		}
		return false;
	}
}
