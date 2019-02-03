package com.snet.util;

import java.util.Map;

public interface EntryPlus<K, V> extends Map.Entry<K, V>, EntryValue<V> {
}
