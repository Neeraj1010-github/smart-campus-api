# Smart Campus Sensor & Room Management API

A RESTful web service built with **JAX-RS (Jersey 3.1)** deployed on **Apache Tomcat 10**. This API manages campus rooms and IoT sensors for the University's Smart Campus initiative.

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
| Apache Tomcat 10 | Servlet container |
| Jackson | JSON serialisation |
| Maven | Build tool |

> **Note:** Spring Boot is NOT used. Only JAX-RS as required.

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
| Room | `LIB-301` | Library Quiet Study, capacity 50 |
| Room | `LAB-101` | Computer Science Lab, capacity 30 |
| Sensor | `TEMP-001` | Temperature, ACTIVE, in LIB-301 |
| Sensor | `CO2-001` | CO2, ACTIVE, in LIB-301 |
| Sensor | `OCC-001` | Occupancy, **MAINTENANCE**, in LAB-101 |

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

In this implementation DELETE is idempotent which means that sending the same DELETE request multiple times will always result in the same outcome on the server. For example if a client sends a DELETE request for a room that exists and has no sensors the room will be deleted and the server will return a 200 OK response. If the same client then sends the exact same DELETE request again the room is already gone so the server will return a 404 Not Found response. Even though the response code is different both times the actual state of the server is the same after each request because the room is absent in both cases. This satisfies the REST idempotency requirement which states that making the same request multiple times should not change the outcome on the server beyond what the first request already did.
---

### Part 3.1 — @Consumes(MediaType.APPLICATION_JSON)
Here are all the remaining answers rewritten in simple easy English:

Part 3.1 — @Consumes
The @Consumes(MediaType.APPLICATION_JSON) annotation tells the endpoint that it will only accept requests that have a Content-Type of application/json. If a client tries to send data in a different format like text/plain or application/xml JAX-RS will automatically reject the request before it even gets to the resource method and will return a 415 Unsupported Media Type error. This means the developer does not need to write any extra code to handle wrong formats because JAX-RS takes care of it automatically at the framework level.

Part 3.2 — @QueryParam vs Path Segment
Using a query parameter like GET /api/v1/sensors?type=CO2 is a better approach for filtering than putting the filter in the URL path like /api/v1/sensors/type/CO2. This is because query parameters are designed for optional filtering of a collection and do not change what resource is being accessed. The resource is still /api/v1/sensors and the type filter just narrows down the results. If the filter was part of the path it would suggest that CO2 is its own separate resource which is not correct. Query parameters are also easier to combine so you could add multiple filters like ?type=CO2&status=ACTIVE without having to change the URL structure.

Part 4.1 — Sub Resource Locator Pattern
The Sub Resource Locator pattern lets a parent resource class hand off a request to a separate child resource class instead of handling everything itself. In this project SensorResource has a method with the @Path annotation for readings but no HTTP method annotation like @GET or @POST. Instead of processing the request directly it returns a SensorReadingResource object and JAX-RS then passes the request to that class to handle. The benefit of doing this is that each class only has one job. SensorResource deals with sensor operations and SensorReadingResource deals with reading history. If everything was put into one single class it would become very large and difficult to manage. Keeping things in separate classes makes the code easier to read test and maintain.

Part 5.2 — 422 vs 404
A 404 Not Found response should be used when the URL being requested does not exist on the server. A 422 Unprocessable Entity response is more appropriate when the URL is correct and the server understands the request but there is a problem with the data inside the request body. In this case when a client tries to register a sensor with a roomId that does not exist the URL /api/v1/sensors is perfectly valid and the JSON is correctly formatted. The problem is that the value of the roomId field points to something that does not exist in the system. Returning 422 makes more sense here because it tells the client that the request was understood but could not be completed because of a data problem inside the body rather than a problem with the URL itself.

Part 5.4 — Security Risk of Stack Traces
Showing raw Java stack traces to people outside the application is a serious security risk. Stack traces contain a lot of sensitive information about how the application works internally. They reveal the exact versions of frameworks and libraries being used which allows attackers to look up known security vulnerabilities for those specific versions. They also show the full file paths and class names inside the application which helps attackers understand how the code is structured. Method names and line numbers can also give away how the business logic works making it easier to find weaknesses. In this project the GlobalExceptionMapper catches any unexpected errors and logs the full details on the server side for developers to see but only sends a simple generic error message back to the client so none of this sensitive information is ever exposed.

Part 5.5 — Filters vs Manual Logging
Using a JAX-RS filter for logging is much better than manually adding log statements inside every single resource method. With a filter you only need to write the logging code once and it will automatically run for every request and response across the entire application. If you added logging manually to each method you would have to update every single method whenever you wanted to change how logging works which is a lot of unnecessary work. There is also a risk of forgetting to add logging to new endpoints when they are created. A filter also keeps the resource methods clean because they only need to contain the business logic and do not have to worry about logging. This makes the code easier to read and maintain overall.

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
