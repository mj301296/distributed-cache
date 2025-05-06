package com.example.distributed_cache.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.distributed_cache.cluster.ClusterManager;
import com.example.distributed_cache.core.CacheService;


@RestController
@RequestMapping("/cache")
public class CacheController {

    private static final Logger logger = LoggerFactory.getLogger(CacheController.class);

    @Autowired
    private CacheService<String, String> cacheService;

    @Autowired
    private ClusterManager clusterManager;

    @PostMapping("/{key}")
    public ResponseEntity<String> put(@PathVariable String key, @RequestParam String value) {
        clusterManager.routePut(key, value);
        return ResponseEntity.ok("Key routed via consistent hashing");
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<String> remove(@PathVariable String key) {
        boolean removed = cacheService.remove(key);
        if (removed) {
            clusterManager.replicateDelete(key);
            return ResponseEntity.ok("Key removed successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Replica endpoints (internal use only)
    @PostMapping("/replica/{key}")
    public ResponseEntity<Void> replicatePut(@PathVariable String key, @RequestParam String value) {
        cacheService.put(key, value);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/replica/{key}")
    public ResponseEntity<Void> replicateDelete(@PathVariable String key) {
        cacheService.remove(key);
        return ResponseEntity.ok().build();
    }
//    @GetMapping("/{key}")
//    public ResponseEntity<String> get(@PathVariable String key) {
//        logger.info("GET request for key: {}", key);
//        String value = cacheService.get(key);
//        return value != null ? ResponseEntity.ok(value) : ResponseEntity.notFound().build();
//    }
    @GetMapping("/{key}")
    public ResponseEntity<String> get(@PathVariable String key) {
        String value = clusterManager.routeGet(key);
        return value != null ? ResponseEntity.ok(value) : ResponseEntity.notFound().build();
    }

    @GetMapping("/replica/{key}")
    public ResponseEntity<String> replicaGet(@PathVariable String key) {
        String value = cacheService.get(key);
        return value != null ? ResponseEntity.ok(value) : ResponseEntity.notFound().build();
    }

//    @PostMapping("/{key}")
//    public ResponseEntity<String> put(@PathVariable String key, @RequestParam String value) {
//        logger.info("POST request to insert key: {}, value: {}", key, value);
//        cacheService.put(key, value);
//        return ResponseEntity.ok("Key inserted successfully");
//    }

    @PostMapping("/{key}/ttl")
    public ResponseEntity<String> putWithTTL(@PathVariable String key, @RequestParam String value, @RequestParam long ttl) {
        logger.info("POST request to insert key: {}, value: {}, TTL: {}", key, value, ttl);
        cacheService.put(key, value, ttl);
        return ResponseEntity.ok("Key inserted with TTL successfully");
    }

//    @DeleteMapping("/{key}")
//    public ResponseEntity<String> remove(@PathVariable String key) {
//        logger.info("DELETE request for key: {}", key);
//        boolean removed = cacheService.remove(key);
//        return removed ? ResponseEntity.ok("Key removed successfully") : ResponseEntity.notFound().build();
//    }

    @DeleteMapping("/clear")
    public ResponseEntity<String> clear() {
        logger.info("DELETE request to clear cache");
        cacheService.clear();
        return ResponseEntity.ok("Cache cleared successfully");
    }

    @GetMapping("/size")
    public ResponseEntity<Integer> size() {
        logger.info("GET request for cache size");
        return ResponseEntity.ok(cacheService.size());
    }
}
