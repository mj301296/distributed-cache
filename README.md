# Distributed Cache with Consistent Hashing 

This project implements a peer-to-peer **distributed in-memory cache** system using Java and Spring Boot. It supports **LRU eviction**, **TTL support**, and **consistent hashing** to distribute keys across multiple nodes efficiently.

---

## 🚀 Features

* In-memory key-value cache with LRU + TTL support
* Peer-to-peer architecture with REST-based replication
* Consistent Hashing for data sharding
* Replica-aware GET and PUT routing
* Thread-safe cache operations

---

## 🏗️ Architecture

Each Spring Boot instance runs:

* A local `CacheService` for in-memory caching
* A `ClusterManager` that uses consistent hashing to route requests
* REST APIs for external and internal cache operations

```
+--------------------+         +--------------------+
| Node 1 (localhost) |  <--->  | Node 2 (localhost) |
| Port: 8080         |         | Port: 8081         |
+--------------------+         +--------------------+
```

---

## 🔧 Setup Instructions

### 1. Clone and Build

```bash
git clone <your-repo-url>
cd distributed-cache
./mvnw clean install
```

### 2. Create Application Profiles

**application-node1.properties**

```properties
server.port=8080
cache.cluster.nodes=http://localhost:8080,http://localhost:8081
```

**application-node2.properties**

```properties
server.port=8081
cache.cluster.nodes=http://localhost:8080,http://localhost:8081
```

### 3. Run Two Nodes

**Terminal 1:**

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=node1
```

**Terminal 2:**

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=node2
```

---

## 📬 REST API Endpoints

### General Endpoints (for clients)

| Method | URL                      | Description                   |
| ------ | ------------------------ | ----------------------------- |
| POST   | `/cache/{key}?value=...` | Add/update a key              |
| GET    | `/cache/{key}`           | Fetch value via hashing route |
| DELETE | `/cache/{key}`           | Remove key and replicate      |

### Internal Replica Endpoints (used by peers)

| Method | URL                    | Description      |
| ------ | ---------------------- | ---------------- |
| POST   | `/cache/replica/{key}` | Peer-to-peer PUT |
| GET    | `/cache/replica/{key}` | Peer-to-peer GET |

---

## 🧠 Future Enhancements

* Add replication to N neighbors for fault tolerance
* Automatic peer discovery
* Metrics dashboard (hit/miss/evictions)
* Docker Compose or Kubernetes support

---

## 🧑‍💻 Author

Mrugank Chandrakant Jadhav

---

## 📜 License

MIT (or specify your preferred license)
