// src/main/java/com/cache/core/model/Node.java
package com.example.distributed_cache.core;

public class Node<K, V> {
    public K key;
    public V value;
    public Node<K, V> prev;
    public Node<K, V> next;
    public long creationTime;
    public long lastAccessTime;
    public int accessCount;
    public long ttl;

    public Node(K key, V value, long ttl) {
        this.key = key;
        this.value = value;
        this.creationTime = System.currentTimeMillis();
        this.lastAccessTime = this.creationTime;
        this.accessCount = 0;
        this.ttl = ttl;
    }

    public boolean isExpired() {
        return ttl > 0 && System.currentTimeMillis() - creationTime > ttl;
    }

    public void resetExpiry() {
        this.creationTime = System.currentTimeMillis();
        this.lastAccessTime = this.creationTime;
    }
}