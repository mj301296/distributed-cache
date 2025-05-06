package com.example.distributed_cache.cluster;

import com.example.distributed_cache.core.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
@Service
public class ClusterManager {

    @Value("${cache.cluster.nodes}")
    private String[] peerNodes;

    @Autowired
    private CacheService<String, String> cacheService;

    private final RestTemplate restTemplate = new RestTemplate();
    private ConsistentHashRing ring;

    @PostConstruct
    public void init() {
        ring = new ConsistentHashRing(Arrays.asList(peerNodes));
    }

    public void routePut(String key, String value) {
        String responsibleNode = ring.getNodeForKey(key);
        if (responsibleNode == null || isSelf(responsibleNode)) {
            // local node is responsible
            cacheService.put(key, value);
            System.out.println("Storing locally: " + key);
        } else {
            try {
                restTemplate.postForObject(responsibleNode + "/cache/replica/" + key + "?value=" + value, null, Void.class);
            } catch (Exception e) {
                System.err.println("Failed to route PUT to " + responsibleNode + ": " + e.getMessage());
            }
        }
    }

    public String routeGet(String key) {
        String targetNode = ring.getNodeForKey(key);
        if (targetNode == null || isSelf(targetNode)) {
            return cacheService.get(key); // Local hit
        } else {
            try {
                return restTemplate.getForObject(targetNode + "/cache/replica/" + key, String.class);
            } catch (Exception e) {
                System.err.println("Failed to route GET to " + targetNode + ": " + e.getMessage());
                return null;
            }
        }
    }

    public void replicateDelete(String key) {
        Arrays.stream(peerNodes).forEach(node -> {
            try {
                restTemplate.delete(node + "/cache/replica/" + key);
            } catch (Exception e) {
                System.err.println("Failed to replicate delete to " + node + ": " + e.getMessage());
            }
        });
    }

    private boolean isSelf(String nodeUrl) {
        return Arrays.stream(peerNodes).noneMatch(peer -> !peer.equals(nodeUrl));
    }
}