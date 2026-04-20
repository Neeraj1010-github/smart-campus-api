# Smart Campus Sensor & Room Management API

A RESTful web service built with **JAX-RS (Jersey 2.41)** and an embedded **Grizzly HTTP server**. This API manages campus rooms and IoT sensors for the University's Smart Campus initiative.

---

## API Overview

The API follows REST architectural principles with a versioned base path of `/api/v1`. It manages three core resources:

- **Rooms** — Physical spaces on campus (e.g., labs, libraries)
- **Sensors** — IoT devices deployed within rooms (temperature, CO2, occupancy)
- **Sensor Readings** — Historical measurement logs per sensor

All data is stored in-memory using `ConcurrentHashMap` and `ArrayList`. No database is used.

### Resource Hierarchy

```
/api/v1
├── /                          → Discovery / API metadata
├── /rooms                     → Room collection
│   └── /{roomId}              → Individual room
└── /sensors                   → Sensor collection
    └── /{sensorId}            → Individual sensor
        └── /readings          → Sensor reading history (sub-resource)
            └── /{readingId}   → Individual reading
```

---

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 11 | Language |
| JAX-RS (Jersey 2.41) | REST framework |
| Grizzly HTTP Server | Embedded servlet container |
| Jackson | JSON serialisation |
| Maven | Build tool |

> **Note:** Spring Boot is NOT used. Only JAX-RS as required.

---

## How to Build and Run

### Prerequisites

- Java 11 or higher
- Maven 3.6+

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/YOUR_USERNAME/smart-campus-api.git
cd smart-campus-api

# 2. Build the fat JAR
mvn clean package

# 3. Run the server
java -jar target/smart-campus-api-1.0-SNAPSHOT.jar
```

The server starts at: **http://localhost:8080/smart-campus-api/api/v1**

You will see output like:
```
INFO: Smart Campus API started at http://localhost:8080/smart-campus-api/api/v1
INFO: Press CTRL+C to stop the server.
```

---

## Sample curl Commands

### 1. Discovery — GET /api/v1
```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1 \
     -H "Accept: application/json"
```

**Expected Response (200 OK):**
```json
{
  "api": "Smart Campus Sensor & Room Management API",
  "version": "1.0.0",
  "contact": "admin@smartcampus.ac.uk",
  "status": "operational",
  "_links": {
    "self": "/api/v1",
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```

---

### 2. Create a Room — POST /api/v1/rooms
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/rooms \
     -H "Content-Type: application/json" \
     -d '{"id":"CS-101","name":"CS Lecture Hall","capacity":120}'
```

**Expected Response (201 Created):**
```json
{
  "message": "Room created successfully.",
  "room": { "id": "CS-101", "name": "CS Lecture Hall", "capacity": 120, "sensorIds": [] },
  "_links": { "self": "/api/v1/rooms/CS-101" }
}
```

---

### 3. Register a Sensor — POST /api/v1/sensors
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
     -H "Content-Type: application/json" \
     -d '{"id":"TEMP-999","type":"Temperature","status":"ACTIVE","currentValue":22.0,"roomId":"LIB-301"}'
```

**Expected Response (201 Created):**
```json
{
  "message": "Sensor registered successfully.",
  "sensor": { "id": "TEMP-999", "type": "Temperature", "status": "ACTIVE", ... },
  "_links": {
    "self": "/api/v1/sensors/TEMP-999",
    "readings": "/api/v1/sensors/TEMP-999/readings"
  }
}
```

---

### 4. Filter Sensors by Type — GET /api/v1/sensors?type=CO2
```bash
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/sensors?type=CO2" \
     -H "Accept: application/json"
```

**Expected Response (200 OK):**
```json
[
  { "id": "CO2-001", "type": "CO2", "status": "ACTIVE", "currentValue": 400.0, "roomId": "LIB-301" }
]
```

---

### 5. Post a Sensor Reading — POST /api/v1/sensors/{sensorId}/readings
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings \
     -H "Content-Type: application/json" \
     -d '{"value":23.7}'
```

**Expected Response (201 Created):**
```json
{
  "message": "Reading recorded successfully.",
  "sensorId": "TEMP-001",
  "reading": { "id": "uuid-...", "timestamp": 1713000000000, "value": 23.7 },
  "updatedSensorValue": 23.7
}
```

---

### 6. Attempt to Delete a Room With Sensors (409 Conflict)
```bash
curl -X DELETE http://localhost:8080/smart-campus-api/api/v1/rooms/LIB-301 \
     -H "Accept: application/json"
```

**Expected Response (409 Conflict):**
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Cannot decommission room 'LIB-301'. It still has 2 active sensor(s) assigned.",
  "hint": "Remove or reassign all sensors from the room before decommissioning it."
}
```

---

### 7. Post a Reading to a MAINTENANCE Sensor (403 Forbidden)
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/OCC-001/readings \
     -H "Content-Type: application/json" \
     -d '{"value":5.0}'
```

**Expected Response (403 Forbidden):**
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Sensor 'OCC-001' is currently in MAINTENANCE mode and cannot accept new readings."
}
```

---

### 8. Register Sensor with Invalid roomId (422 Unprocessable Entity)
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
     -H "Content-Type: application/json" \
     -d '{"id":"TEMP-XYZ","type":"Temperature","status":"ACTIVE","currentValue":0.0,"roomId":"FAKE-999"}'
```

**Expected Response (422 Unprocessable Entity):**
```json
{
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "The roomId 'FAKE-999' does not exist.",
  "hint": "Ensure the referenced roomId exists before registering a sensor."
}
```

---

## Report: Question Answers

### Part 1.1 — JAX-RS Resource Lifecycle

By default, JAX-RS creates a **new instance of each resource class for every incoming HTTP request** (per-request lifecycle). This means fields are not shared between requests, so storing mutable state directly in a resource class field would result in data being lost after each request completes.

To manage shared in-memory state across requests, this project uses the **Singleton pattern** via `DataStore.getInstance()`. The `DataStore` class is instantiated once and holds `ConcurrentHashMap` collections. `ConcurrentHashMap` is used instead of `HashMap` because multiple requests may arrive concurrently, and plain `HashMap` is not thread-safe — simultaneous reads and writes would cause data corruption or `ConcurrentModificationException`. The singleton approach ensures all resource instances operate on the same shared data while the concurrent map handles race conditions safely.

---

### Part 1.2 — HATEOAS (Hypermedia as the Engine of Application State)

HATEOAS is considered a hallmark of advanced RESTful design because it makes an API **self-describing at runtime**. Rather than relying on external documentation to know what actions are available, clients can discover valid next steps directly from the API's responses — following links embedded in the JSON (e.g., `"_links": { "readings": "/api/v1/sensors/TEMP-001/readings" }`).

For client developers, HATEOAS reduces coupling: clients do not need to hardcode URLs or memorise the entire URI structure. If the API evolves and a path changes, clients following hypermedia links automatically adapt. Static documentation becomes stale, but hypermedia-driven responses are always current and contextually accurate to the current resource state.

---

### Part 2.1 — Returning IDs vs Full Room Objects

Returning only **IDs** in a room list reduces payload size significantly when there are thousands of rooms, lowering bandwidth consumption and improving response time. However, clients must then make a separate request per room to fetch details, increasing the total number of HTTP round-trips (the N+1 query problem).

Returning **full room objects** means clients have all the information they need in a single request — ideal for dashboards or admin panels that display room details immediately. The trade-off is a larger payload. In practice, a good RESTful design may offer both: a lightweight list endpoint returning summaries, and a detail endpoint for individual resources. This project returns full objects by default since the data set is campus-scale and clients typically need room metadata.

---

### Part 2.2 — Idempotency of DELETE

In this implementation, DELETE **is idempotent** in the standard REST sense: the server state after repeated identical DELETE requests is the same. Specifically:

- **First DELETE:** If the room exists and has no sensors, it is removed. Returns `200 OK`.
- **Second DELETE (same roomId):** The room is already gone. Returns `404 Not Found`.

The server state (room absent) is identical after both calls. The HTTP response code differs (200 vs 404), but the resource state does not change on subsequent calls — the room remains absent. This satisfies REST's idempotency contract: multiple identical requests produce the same server-side outcome.

---

### Part 3.1 — @Consumes(MediaType.APPLICATION_JSON)

The `@Consumes(MediaType.APPLICATION_JSON)` annotation declares that the endpoint only accepts requests with a `Content-Type: application/json` header. If a client sends data in a different format — for example `text/plain` or `application/xml` — JAX-RS will automatically reject the request before it ever reaches the resource method, returning an **HTTP 415 Unsupported Media Type** response. This is handled at the framework level, protecting the resource method from receiving unexpected data formats that Jackson cannot deserialise into the expected Java POJO.

---

### Part 3.2 — @QueryParam vs Path Segment for Filtering

Using `@QueryParam` (e.g., `GET /api/v1/sensors?type=CO2`) is considered superior for filtering because query parameters are semantically designed for **optional, non-hierarchical refinements** of a collection. The resource being addressed is still `/api/v1/sensors` — the type filter simply narrows the result set.

A path segment approach (e.g., `/api/v1/sensors/type/CO2`) implies `type/CO2` is a distinct sub-resource, which is semantically incorrect — it is not a resource, it is a filter criterion. Query parameters also compose more naturally: multiple filters can be chained (`?type=CO2&status=ACTIVE`) without altering the URL structure. Path segments for filtering also break caching and bookmarking behaviour since the URL no longer identifies the canonical resource.

---

### Part 4.1 — Sub-Resource Locator Pattern

The Sub-Resource Locator pattern allows a parent resource class to **delegate routing** to a dedicated child resource class at runtime. In this project, `SensorResource` has a locator method annotated with `@Path("/{sensorId}/readings")` that returns a `SensorReadingResource` instance — without any HTTP method annotation. JAX-RS then forwards the request to that instance for further processing.

The architectural benefit is **separation of concerns**: `SensorResource` handles sensor CRUD, while `SensorReadingResource` focuses exclusively on reading history. In large APIs with deep nesting (e.g., `/sensors/{id}/readings/{rid}/annotations`), keeping all paths in a single controller creates an unmaintainable monolith. Delegation to dedicated classes makes each class independently testable, readable, and maintainable — a direct application of the Single Responsibility Principle.

---

### Part 5.2 — HTTP 422 vs HTTP 404 for Missing Reference

HTTP **404 Not Found** should be used when the **requested resource URL** does not exist — i.e., the path itself is unresolvable. HTTP **422 Unprocessable Entity** is more semantically accurate when the URL is valid and the server understands the request, but the **content of the payload** contains a logical error — in this case, a `roomId` field that references a non-existent room.

The request to `POST /api/v1/sensors` is perfectly valid syntactically. The JSON is well-formed and the endpoint exists. The problem is semantic: a value *inside* the body refers to something that cannot be resolved. 422 communicates precisely this — "I understood what you sent, but I cannot process it because of a data integrity issue inside the payload." This gives client developers a more actionable error signal.

---

### Part 5.4 — Security Risk of Exposing Stack Traces

Exposing raw Java stack traces to external API consumers is a significant security vulnerability for several reasons:

1. **Technology fingerprinting:** Stack traces reveal the exact framework (Jersey, Grizzly), Java version, and internal package structure (e.g., `com.smartcampus.resource.RoomResource`). Attackers use this to target known CVEs for those specific versions.

2. **Internal path disclosure:** Stack traces expose the full file system path and class hierarchy of the application, helping attackers map the internal architecture.

3. **Business logic exposure:** Line numbers and method names in traces reveal how the application is structured internally, making it easier to identify logic flaws or injection points.

4. **Dependency enumeration:** Third-party library names and versions in traces allow attackers to cross-reference public vulnerability databases (NVD, CVE) for known exploits.

The `GlobalExceptionMapper` in this project logs the full trace server-side for developers while returning only a generic `500 Internal Server Error` message to the client — eliminating all of the above risks.

---

### Part 5.5 — JAX-RS Filters vs Manual Logging in Resource Methods

Using JAX-RS filters for cross-cutting concerns like logging is superior to manually inserting `Logger.info()` calls in every resource method for several reasons:

1. **No code duplication:** A single filter class handles logging for every endpoint automatically. Adding it manually to 10+ resource methods violates the DRY (Don't Repeat Yourself) principle and creates a maintenance burden.

2. **Separation of concerns:** Resource methods should contain only business logic. Mixing in infrastructure concerns (logging, authentication, CORS headers) makes them harder to read and test.

3. **Consistency:** A filter guarantees every request and response is logged uniformly. Manual logging is prone to being forgotten on new endpoints.

4. **Easier to extend:** To change the log format or add request tracing IDs, only the filter needs updating — not every resource method across the codebase.

---

## Project Structure

```
smart-campus-api/
├── pom.xml
└── src/main/java/com/smartcampus/
    ├── application/
    │   ├── DataStore.java                  # Singleton in-memory data store
    │   ├── Main.java                       # Grizzly server entry point
    │   └── SmartCampusApplication.java     # JAX-RS @ApplicationPath config
    ├── model/
    │   ├── Room.java
    │   ├── Sensor.java
    │   └── SensorReading.java
    ├── resource/
    │   ├── DiscoveryResource.java          # GET /api/v1
    │   ├── RoomResource.java               # /api/v1/rooms
    │   ├── SensorResource.java             # /api/v1/sensors
    │   └── SensorReadingResource.java      # /api/v1/sensors/{id}/readings
    ├── exception/
    │   ├── RoomNotEmptyException.java
    │   ├── RoomNotEmptyExceptionMapper.java        # 409
    │   ├── LinkedResourceNotFoundException.java
    │   ├── LinkedResourceNotFoundExceptionMapper.java  # 422
    │   ├── SensorUnavailableException.java
    │   ├── SensorUnavailableExceptionMapper.java   # 403
    │   └── GlobalExceptionMapper.java              # 500 catch-all
    └── filter/
        └── LoggingFilter.java              # Request & response logging
```

---

## Pre-seeded Test Data

The API starts with the following data for immediate testing:

| Type | ID | Details |
|---|---|---|
| Room | `LIB-301` | Library Quiet Study, capacity 50 |
| Room | `LAB-101` | Computer Science Lab, capacity 30 |
| Sensor | `TEMP-001` | Temperature, ACTIVE, in LIB-301 |
| Sensor | `CO2-001` | CO2, ACTIVE, in LIB-301 |
| Sensor | `OCC-001` | Occupancy, **MAINTENANCE**, in LAB-101 |
 parameters also compose naturally: ?type=CO2&status=ACTIVE without altering URL structure.

### Part 4.1 — Sub-Resource Locator Pattern

The Sub-Resource Locator pattern allows a parent resource class to delegate routing to a dedicated child resource class at runtime. In this project, SensorResource has a locator method annotated with @Path("/{sensorId}/readings") that returns a SensorReadingResource instance — without any HTTP method annotation. JAX-RS forwards the request to that instance for further processing.

The benefit is separation of concerns: SensorResource handles sensor CRUD while SensorReadingResource focuses exclusively on reading history. Keeping all paths in a single controller creates an unmaintainable monolith. Delegation to dedicated classes makes each class independently testable and readable — a direct application of the Single Responsibility Principle.

### Part 5.2 — HTTP 422 vs HTTP 404

HTTP 404 should be used when the requested resource URL does not exist. HTTP 422 Unprocessable Entity is more semantically accurate when the URL is valid and the server understands the request, but the content of the payload contains a logical error — in this case a roomId field referencing a non-existent room.

The request to POST /api/v1/sensors is perfectly valid syntactically. The JSON is well-formed and the endpoint exists. The problem is semantic — a value inside the body refers to something that cannot be resolved. 422 communicates precisely this — "I understood what you sent but cannot process it because of a data integrity issue inside the payload."

### Part 5.4 — Security Risks of Exposing Stack Traces

Exposing raw Java stack traces to external API consumers is a significant security vulnerability:

1. Technology fingerprinting: reveals exact framework versions (Jersey 3.1, Java 11) so attackers can target known CVEs for those versions.
2. Internal path disclosure: exposes full file system paths and class hierarchy helping attackers map the internal architecture.
3. Business logic exposure: line numbers and method names reveal how the application works internally making it easier to find exploits.
4. Dependency enumeration: third-party library names allow attackers to cross-reference public vulnerability databases for known exploits.

The GlobalExceptionMapper logs the full trace server-side for developers while returning only a generic 500 message to the client — eliminating all of the above risks.

### Part 5.5 — JAX-RS Filters vs Manual Logging

Using JAX-RS filters for cross-cutting concerns like logging is superior to manually inserting Logger.info() calls in every resource method because:

1. No code duplication: one filter handles logging for every endpoint automatically. Adding it manually to 10+ resource methods violates DRY (Don't Repeat Yourself).
2. Separation of concerns: resource methods should contain only business logic. Mixing in infrastructure concerns makes them harder to read and test.
3. Consistency: a filter guarantees every request and response is logged uniformly. Manual logging is prone to being forgotten on new endpoints.
4. Easier to extend: to change the log format, only the filter needs updating — not every resource method across the codebase.

---

## Project Structure

```
smart-campus-api/
├── pom.xml
├── src/main/
│   ├── java/com/smartcampus/
│   │   ├── application/
│   │   │   ├── DataStore.java
│   │   │   └── SmartCampusApplication.java
│   │   ├── model/
│   │   │   ├── Room.java
│   │   │   ├── Sensor.java
│   │   │   └── SensorReading.java
│   │   ├── resource/
│   │   │   ├── DiscoveryResource.java
│   │   │   ├── RoomResource.java
│   │   │   ├── SensorResource.java
│   │   │   └── SensorReadingResource.java
│   │   ├── exception/
│   │   │   ├── RoomNotEmptyException.java
│   │   │   ├── RoomNotEmptyExceptionMapper.java
│   │   │   ├── LinkedResourceNotFoundException.java
│   │   │   ├── LinkedResourceNotFoundExceptionMapper.java
│   │   │   ├── SensorUnavailableException.java
│   │   │   ├── SensorUnavailableExceptionMapper.java
│   │   │   └── GlobalExceptionMapper.java
│   │   └── filter/
│   │       └── LoggingFilter.java
│   └── webapp/WEB-INF/
│       └── web.xml
└── README.md
```
