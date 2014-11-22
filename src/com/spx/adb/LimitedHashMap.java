package com.spx.adb;

import java.util.LinkedHashMap;

public class LimitedHashMap<K,V> extends LinkedHashMap<K,V> {
    private int capacity = 100;

    public LimitedHashMap(int maxCapacity) {
        capacity = maxCapacity;
    }

    @Override
    protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
        return size() > capacity;
    }

    
}
