package com.snet.util;

import java.util.Objects;

@SuppressWarnings("rawtypes")
public interface KeyEqualFunc<E> {
	KeyEqualFunc DEF_EQUAL = Objects::equals;
	KeyEqualFunc IDENTITY_EQUAL = (key, obj) -> key == obj;

	boolean equals(E key, Object obj);
}
