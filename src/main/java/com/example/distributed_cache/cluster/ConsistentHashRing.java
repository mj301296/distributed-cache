package com.example.distributed_cache.cluster;

import java.util.*;

public class ConsistentHashRing {
    private final SortedMap<Integer, String> ring = new TreeMap<>();
    private final int virtualNodes = 100;

    public ConsistentHashRing(List<String> nodes) {
        for (String node : nodes) {
            for (int i = 0; i < virtualNodes; i++) {
                int hash = hash(node + "#" + i);
                ring.put(hash, node);
            }
        }
    }

    public String getNodeForKey(String key) {
        if (ring.isEmpty()) return null;
        int hash = hash(key);
        if (!ring.containsKey(hash)) {
            SortedMap<Integer, String> tailMap = ring.tailMap(hash);
            hash = tailMap.isEmpty() ? ring.firstKey() : tailMap.firstKey();
        }
        return ring.get(hash);
    }

    private int hash(String key) {
        return key.hashCode() & 0x7fffffff;
    }
}