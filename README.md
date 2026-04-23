# Smart Campus Sensor & Room Management API

A RESTful web API built with **JAX-RS (Jersey 3.1)** deployed on **Apache Tomcat 10**. This API manages campus rooms and IoT sensors for the University's Smart Campus initiative.

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
| Java 11 or higher| Language |
| JAX-RS (Jersey 3.1) | REST framework |
| Apache Tomcat 10 | Server deployed on |
| Jackson | JSON serialisation |
| Maven | Build tool |


---

## How to Build and Run

### Prerequisites

- Java 11 or higher
- Maven 3.6+
- Apache Tomcat 10

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/Neeraj1010-github/smart-campus-api.git
cd smart-campus-api

# 2. Build the WAR file
mvn clean package

# 3. Copy WAR to Tomcat webapps folder
cp target/smart-campus-api-1.0-SNAPSHOT.war /path/to/tomcat/webapps/smart-campus-api.war

# 4. Start Tomcat
```

Or in NetBeans:
1. Open the project
2. Right-click → Run (with Apache Tomcat selected as server)

The API will be available at: **http://localhost:8080/smart-campus-api/api/v1**

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
  "description": "RESTful API for managing campus rooms and IoT sensors.",
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

### 2. Get all rooms — GET /api/v1/rooms
```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/rooms \
     -H "Accept: application/json"
```

---

### 3. Create a Room — POST /api/v1/rooms
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/rooms \
     -H "Content-Type: application/json" \
     -d '{"id":"CS-205","name":"Computer Science Seminar Room","capacity":25}'
```

**Expected Response (201 Created):**
```json
{
  "message": "Room created successfully.",
  "room": { "id": "CS-205", "name": "Computer Science Seminar Room", "capacity": 25, "sensorIds": [] },
  "_links": { "self": "/api/v1/rooms/CS-205" }
}
```

---

### 4. Register a Sensor — POST /api/v1/sensors
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
     -H "Content-Type: application/json" \
     -d '{"id":"TEMP-999","type":"Temperature","status":"ACTIVE","currentValue":22.0,"roomId":"LIB-301"}'
```

---

### 5. Filter Sensors by Type — GET /api/v1/sensors?type=CO2
```bash
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/sensors?type=CO2" \
     -H "Accept: application/json"
```

---

### 6. Post a Sensor Reading — POST /api/v1/sensors/{sensorId}/readings
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings \
     -H "Content-Type: application/json" \
     -d '{"value":23.7}'
```

---

### 7. Delete a Room With Sensors (409 Conflict)
```bash
curl -X DELETE http://localhost:8080/smart-campus-api/api/v1/rooms/LIB-301 \
     -H "Accept: application/json"
```

**Expected Response (409 Conflict):**
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Cannot decommission room 'LIB-301'. It still has 2 sensor(s) assigned: [TEMP-001, CO2-001]",
  "hint": "Remove or reassign all sensors from the room before decommissioning it."
}
```

---

### 8. Post Reading to MAINTENANCE Sensor (403 Forbidden)
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/OCC-001/readings \
     -H "Content-Type: application/json" \
     -d '{"value":5.0}'
```

---

### 9. Register Sensor with Invalid roomId (422)
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
     -H "Content-Type: application/json" \
     -d '{"id":"TEMP-000","type":"Temperature","status":"ACTIVE","currentValue":0.0,"roomId":"FAKE-999"}'
```

---

### 10. Get Reading History
```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings \
     -H "Accept: application/json"
```

---

## Pre-seeded Test Data

| Type | ID | Details |
|---|---|---|
| Room | LIB-301 | Library Quiet Study, capacity 50 |
| Room | LAB-101 | Computer Science Lab, capacity 30 |
| Sensor | TEMP-001 | Temperature, ACTIVE, in LIB-301 |
| Sensor | CO2-001 | CO2, ACTIVE, in LIB-301 |
| Sensor | OCC-001 | Occupancy, **MAINTENANCE**, in LAB-101 |

---

## Report: Question Answers

### Part 1.1 — JAX-RS Resource Lifecycle
JAX-RS creates a new instance of each resource class for every incoming HTTP request which means any data stored inside a resource class is lost the moment the request finishes. To fix this the project uses the Singleton pattern through DataStore.getInstance() which creates one single shared object when the server starts that stays alive for as long as the server is running. All resource classes access this same object so data saved during one request is still there for the next one. Inside the DataStore a ConcurrentHashMap is used instead of a regular HashMap because the server handles multiple requests at the same time and a regular HashMap is not safe for this as two threads accessing it at the same time can cause data corruption or crashes. ConcurrentHashMap handles multiple threads safely which means all resource classes can read and write data at the same time without any problems.

---

### Part 1.2 — HATEOAS (Hypermedia as the Engine of Application State)

HATEOAS is seen as a key feature of advanced REST design because it allows an API to describe itself while it is being used. Instead of having to read through external documentation to find out what actions are available clients can simply look at the links included in the API response and follow them to find out what they can do next. This makes things much easier for developers building applications that use the API because they do not need to remember or hardcode any URLs into their code. If the API changes and a URL is updated the client will automatically get the new link in the response and will not break. Unlike written documentation which can become outdated over time the links returned by the API are always accurate and up to date because they come directly from the server.

---

### Part 2.1 — Returning IDs vs Full Room Objects

When returning a list of rooms there are two options to consider. Returning only the IDs of each room keeps the response small and reduces the amount of data sent over the network which makes it faster. However the problem with this is that the client then has to send a separate request for every single room to get its details which means a lot of extra requests are being made. This is known as the N+1 problem. On the other hand returning the full room objects means the client gets everything it needs in just one request which is much better for things like dashboards or admin panels that need to show room details straight away. The downside is that the response will be bigger and use more bandwidth. This project returns full room objects by default because in most cases the client will need the room details immediately and it is better to get everything in one go rather than making multiple requests.

---

### Part 2.2 — Idempotency of DELETE

In this implementation DELETE is idempotent which meansthat sending the same DELETE request multiple times will always result in the same outcome on the server. For example if a client sends a DELETE request for a room that exists and has no sensors the room will be deleted and the server will return a 200 OK response. If the same client then sends the exact same DELETE request again the room is already gone so the server will return a 404 Not Found response. Even though the response code is different both times the actual state of the server is the same after each request because the room is absent in both cases. This satisfies the REST idempotency requirement which states that making the same request multiple times should not change the outcome on the server beyond what the first request already did.

---

### Part 3.1 — @Consumes(MediaType.APPLICATION_JSON)

Adding @Consumes(MediaType.APPLICATION_JSON) to an endpoint means it will reject anything that isn't sent as JSON. So if a client accidentally sends a plain text or application/xml, JAX-RS catches it before the request even makes it to the resource method and gives back a 415 Unsupported Media Type. The developer never has to write a single line of code to deal with bad formats because the framework already handles it.

### Part 3.2 — @QueryParam vs Path Segment
Filtering through a query parameter like GET /api/v1/sensors?type=CO2 makes far more sense than burying the filter inside the path itself like /api/v1/sensors/type/CO2. The path should identify a resource and CO2 is not a resource, it is just a way of narrowing down a list. Keeping it as a query parameter means the endpoint stays as /api/v1/sensors and the filter sits on top without changing what is actually being accessed. Another  benefit is that query parameters stack easily. If you wanted to filter by both type and status you could just write ?type=CO2&status=ACTIVE and it works straight away without touching the URL structure at all.

### Part 4.1 — Sub Resource Locator Pattern
Rather than cramming every request into one class, the Sub Resource Locator pattern lets a parent resource pass certain requests off to a dedicated child class. In this project, SensorResource has a method marked with @Path for readings but no HTTP  like @GET or @POST attached to it. That method does not process the request itself, it just hands back a SensorReadingResource object and lets JAX-RS take it from there. The end result is that each class stays focused on one thing. SensorResource handles sensor logic, SensorReadingResource handles reading history. Throwing everything into a single class would have made it hard to follow. Keeping them separate means the code is cleaner, easier to test and far simpler to update further on.

### Part 5.2 — 422 vs 404
404 and 422 are used for alot of different purposes but they cover completely different situations. A 404 means the URL itself does not exist on the server. A 422 means the URL is fine, the server understood the request, but something inside the request body is wrong. In this case the endpoint /api/v1/sensors is a real, valid route and the JSON being sent is correctly structured. The issue is specifically that the roomId value points to a room that does not exist in the system. Sending back a 404 here would be misleading because the URL works perfectly. A 422 is the honest response as it tells the client that everything about the request was understood but it could not go through because of a problem with the data itself.

### Part 5.4 — Security Risk of Stack Traces
Exposing raw Java stack traces to anyone outside the application is a serious mistake. They give away far more than most people realise. The framework and library versions are visible, which means an attacker can immediately go and look up known vulnerabilities for those exact versions. Full file paths and class names are also exposed, giving a clear picture of how the code is laid out. On top of that, method names and line numbers can reveal how the business logic actually works, making it much easier to spot where things could be exploited. In this project the GlobalExceptionMapper acts as a safety net, catching any unexpected errors, logging the full details server side where only developers can see them, and sending the client nothing more than a plain generic message.

### Part 5.5 — Filters vs Manual Logging
A JAX-RS filter is a much cleaner way to handle logging than scattering log statements across every resource method. You write the logic once and it automatically covers every request and response in the application without any extra effort. Doing it manually means every method needs its own log statement, and any time the logging behaviour needs changing you have to go through each one individually which is very long and there is also a high risk of errors being made. There is also the very real chance of simply forgetting to add logging to a new endpoint when it gets created. On top of that, keeping logging out of the resource methods means they only contain what they are actually supposed to, the business logic. The code ends up cleaner, easier to follow and much simpler to maintain.

## Project Structure

```
smart-campus-api/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/smartcampus/
    │   ├── application/
    │   │   ├── DataStore.java                  # Singleton in-memory data store
    │   │   └── SmartCampusApplication.java     # JAX-RS @ApplicationPath config
    │   ├── model/
    │   │   ├── Room.java
    │   │   ├── Sensor.java
    │   │   └── SensorReading.java
    │   ├── resource/
    │   │   ├── DiscoveryResource.java          # GET /api/v1
    │   │   ├── RoomResource.java               # /api/v1/rooms
    │   │   ├── SensorResource.java             # /api/v1/sensors
    │   │   └── SensorReadingResource.java      # /api/v1/sensors/{id}/readings
    │   ├── exception/
    │   │   ├── RoomNotEmptyException.java
    │   │   ├── RoomNotEmptyExceptionMapper.java        # 409
    │   │   ├── LinkedResourceNotFoundException.java
    │   │   ├── LinkedResourceNotFoundExceptionMapper.java  # 422
    │   │   ├── SensorUnavailableException.java
    │   │   ├── SensorUnavailableExceptionMapper.java   # 403
    │   │   └── GlobalExceptionMapper.java              # 500 catch-all
    │   └── filter/
    │       └── LoggingFilter.java              # Request & response logging
    └── webapp/
        ├── META-INF/
        │   └── context.xml
        └── WEB-INF/
            └── web.xml
```
