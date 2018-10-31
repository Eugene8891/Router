package com.midrive.voice.router_api.core;

import java.util.TreeMap;

/**
 * TreeMap with unique key.
 *
 */
public class UniqueKeyTreeMap<K, V> extends TreeMap<K, V> {
    private String errorMsg;

    public UniqueKeyTreeMap(String exceptionText) {
        super();
        errorMsg = exceptionText;
    }

    @Override
    public V put(K key, V value) {
        if (containsKey(key)) {
            throw new RuntimeException(String.format(errorMsg, key));
        } else {
            return super.put(key, value);
        }
    }
}
