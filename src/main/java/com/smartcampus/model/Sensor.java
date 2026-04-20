package com.smartcampus.model;

/**
 * Sensor.java - Data model representing an IoT sensor deployed in a campus room.
 *
 * Each sensor belongs to exactly one room (via roomId).
 * The status field controls what operations are allowed on the sensor:
 *   - "ACTIVE"      : sensor is working normally, can accept new readings
 *   - "MAINTENANCE" : sensor is being serviced, cannot accept new readings (returns 403)
 *   - "OFFLINE"     : sensor is disconnected but can still accept readings
 *
 * Example JSON representation:
 * {
 *   "id": "TEMP-001",
 *   "type": "Temperature",
 *   "status": "ACTIVE",
 *   "currentValue": 21.5,
 *   "roomId": "LIB-301"
 * }
 */
public class Sensor {

    // Unique identifier, e.g. "TEMP-001", "CO2-042"
    private String id;

    // Category of sensor, e.g. "Temperature", "CO2", "Occupancy"
    private String type;

    // Current operational state: "ACTIVE", "MAINTENANCE", or "OFFLINE"
    private String status;

    // The most recent measurement recorded by this sensor.
    // This is updated automatically every time a new reading is posted.
    private double currentValue;

    // Foreign key linking this sensor to the room it is physically located in.
    // Must match a valid Room ID in the system.
    private String roomId;

    // No-argument constructor required by Jackson for JSON deserialisation
    public Sensor() {}

    // Convenience constructor for pre-seeding test data
    public Sensor(String id, String type, String status, double currentValue, String roomId) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.currentValue = currentValue;
        this.roomId = roomId;
    }

    // Getters and setters - required by Jackson to read/write each field as JSON
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getCurrentValue() { return currentValue; }
    public void setCurrentValue(double currentValue) { this.currentValue = currentValue; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
}
