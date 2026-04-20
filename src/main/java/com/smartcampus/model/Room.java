package com.smartcampus.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Room.java - Data model representing a physical room on campus.
 *
 * This is a POJO (Plain Old Java Object) - it just holds data.
 * Jackson (our JSON library) uses the getters/setters to automatically
 * convert this object to/from JSON when the API sends or receives data.
 *
 * Example JSON representation:
 * {
 *   "id": "LIB-301",
 *   "name": "Library Quiet Study",
 *   "capacity": 50,
 *   "sensorIds": ["TEMP-001", "CO2-001"]
 * }
 */
public class Room {

    // Unique identifier for the room, e.g. "LIB-301"
    private String id;

    // Human-readable name, e.g. "Library Quiet Study"
    private String name;

    // Maximum number of people allowed in the room
    private int capacity;

    // List of sensor IDs deployed in this room.
    // Initialised as empty list so it is never null.
    private List<String> sensorIds = new ArrayList<>();

    // No-argument constructor required by Jackson for JSON deserialisation
    public Room() {}

    // Convenience constructor for pre-seeding test data
    public Room(String id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }

    // Getters and setters - required by Jackson to read/write each field as JSON
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public List<String> getSensorIds() { return sensorIds; }
    public void setSensorIds(List<String> sensorIds) { this.sensorIds = sensorIds; }
}
