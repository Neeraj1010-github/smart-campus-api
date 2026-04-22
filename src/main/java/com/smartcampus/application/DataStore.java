package com.smartcampus.application;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DataStore.java - The in-memory "database" for the entire application.
 *
 * WHY A SINGLETON?
 * By default, JAX-RS creates a brand new instance of each resource class
 * (e.g. RoomResource, SensorResource) for every incoming HTTP request.
 * This means we can't store data inside fields in resource classes
 *

 *
 * 
 * Multiple HTTP requests can arrive at the same time (concurrently).
 * A regular HashMap is not thread-safe - simultaneous reads and writes
 * can cause data corruption or crashes. ConcurrentHashMap handles this safely.
 *
 * 
 */
public class DataStore {

    // The single instance of DataStore - created once when the class is loaded
    private static final DataStore INSTANCE = new DataStore();

    // Stores all rooms, keyed by room ID (e.g. "LIB-301" -> Room object)
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    // Stores all sensors, keyed by sensor ID (e.g. "TEMP-001" -> Sensor object)
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();

    // Stores reading history per sensor, keyed by sensor ID
    // e.g. "TEMP-001" -> [reading1, reading2, reading3, ...]
    private final Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    // Private constructor prevents anyone from calling "new DataStore()"
    private DataStore() {
        seedData(); // populate with some initial test data on startup
    }

    /**
     * The global access point - call this from any resource class to get the shared store.
     */
    public static DataStore getInstance() {
        return INSTANCE;
    }

    /**
     * Pre-populates the store with sample data so the API is immediately testable
     * without needing to create everything from scratch via POST requests.
     */
    private void seedData() {
        // Create two sample rooms
        Room r1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room r2 = new Room("LAB-101", "Computer Science Lab", 30);
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);

        // Create three sample sensors
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 21.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-001",  "CO2",         "ACTIVE", 400.0, "LIB-301");
        Sensor s3 = new Sensor("OCC-001",  "Occupancy",   "MAINTENANCE", 0.0, "LAB-101");
        sensors.put(s1.getId(), s1);
        sensors.put(s2.getId(), s2);
        sensors.put(s3.getId(), s3);

        // Link sensors to their rooms (add sensor IDs to the room's sensorIds list)
        r1.getSensorIds().add(s1.getId()); // TEMP-001 is in LIB-301
        r1.getSensorIds().add(s2.getId()); // CO2-001 is in LIB-301
        r2.getSensorIds().add(s3.getId()); // OCC-001 is in LAB-101

        // Initialise empty reading history lists for each sensor
        sensorReadings.put("TEMP-001", new ArrayList<>());
        sensorReadings.put("CO2-001",  new ArrayList<>());
        sensorReadings.put("OCC-001",  new ArrayList<>());

        // Add one initial reading per active sensor
        sensorReadings.get("TEMP-001").add(new SensorReading(21.5));
        sensorReadings.get("CO2-001").add(new SensorReading(400.0));
    }

    // Getters so resource classes can access the shared maps
    public Map<String, Room> getRooms()                          { return rooms; }
    public Map<String, Sensor> getSensors()                      { return sensors; }
    public Map<String, List<SensorReading>> getSensorReadings()  { return sensorReadings; }
}
