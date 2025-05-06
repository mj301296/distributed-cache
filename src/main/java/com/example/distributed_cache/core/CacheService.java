package com.example.distributed_cache.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class CacheService<K, V> implements DistributedCache<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);
    private final int capacity;
    private final Map<K, Node<K, V>> map;
    private final Node<K, V> lru;
    private final Node<K, V> mru;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public CacheService() {
        this.capacity = 100; // Default capacity, can be configured
        this.map = new LinkedHashMap<>();
        this.lru = new Node<>(null, null, Long.MAX_VALUE);
        this.mru = new Node<>(null, null, Long.MAX_VALUE);
        lru.next = mru;
        mru.prev = lru;
        logger.info("CacheService initialized with capacity: {}", capacity);
    }

    @Override
    public V get(K key) {
        lock.readLock().lock();
        try {
            logger.debug("Fetching key: {}", key);
            if (!map.containsKey(key)) {
                logger.warn("Key not found: {}", key);
                return null;
            }
            Node<K, V> node = map.get(key);
            lock.readLock().unlock();
            lock.writeLock().lock();
            try {
                remove(key);
                insert(node);
                logger.info("Key accessed: {}, Value: {}", key, node.value);
                return node.value;
            } finally {
                lock.writeLock().unlock();
            }
        } finally {
            if (lock.getReadHoldCount() > 0) {
                lock.readLock().unlock();
            }
        }
    }

    @Override
    public void put(K key, V value) {
        lock.writeLock().lock();
        try {
            logger.debug("Inserting key: {}, value: {}", key, value);
            Node<K,V> node = null; 
            if(map.containsKey(key))
            	node = map.get(key);
            else
            	node = new Node<>(key, value, Long.MAX_VALUE);
            remove(key);
            insert(node);
            logger.info("Key inserted: {}", key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void put(K key, V value, long ttlMillis) {
        lock.writeLock().lock();
        try {
            logger.debug("Inserting key: {}, value: {}, TTL: {}", key, value, ttlMillis);
            Node<K,V> node = null; 
            if(map.containsKey(key))
            	node = map.get(key);
            else
            	node = new Node<>(key, value, ttlMillis);
            remove(key);
            insert(node);
            logger.info("Key inserted with TTL: {}", key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean remove(K key) {
        lock.writeLock().lock();
        try {
            logger.debug("Removing key: {}", key);
            if (!map.containsKey(key)) {
                logger.warn("Key not found for removal: {}", key);
                return false;
            }
            Node<K, V> node = map.get(key);
            Node<K, V> nextNode = node.next;
            Node<K, V> prevNode = node.prev;
            nextNode.prev = prevNode;
            prevNode.next = nextNode;
            map.remove(key);
            logger.info("Key removed: {}", key);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void insert(Node<K, V> node) {
        lock.writeLock().lock();
        try {
            logger.debug("Inserting node with key: {}", node.key);
            Node<K, V> currLru = lru.next;
            currLru.prev = node;
            node.next = currLru;
            lru.next = node;
            node.prev = lru;
            map.put(node.key, node);
            if (map.size() > capacity) {
                logger.warn("Cache capacity exceeded. Removing LRU key: {}", mru.prev.key);
                remove(mru.prev.key);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            logger.info("Clearing cache...");
            map.clear();
            lru.next = mru;
            mru.prev = lru;
            logger.info("Cache cleared.");
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void invalidate(K key) {
        lock.writeLock().lock();
        try {
            logger.debug("Invalidating key: {}", key);
            remove(key);
            logger.info("Key invalidated: {}", key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public int size() {
        lock.readLock().lock();
        try {
            logger.debug("Fetching cache size.");
            return map.size();
        } finally {
            lock.readLock().unlock();
        }
    }
}
