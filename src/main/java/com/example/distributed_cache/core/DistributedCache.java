package com.example.distributed_cache.core;

public interface DistributedCache<K, V> {
    V get(K key);

    void put(K key, V value);

    void put(K key, V value, long ttlMillis);

    boolean remove(K key);

    void clear();

    void invalidate(K key);

    int size();
}