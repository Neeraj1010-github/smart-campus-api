package com.smartcampus.model;

import java.util.UUID;

/**
 * SensorReading.java - Represents a single historical measurement from a sensor.
 *
 * Every time a sensor records a measurement, a SensorReading is created and
 * stored in the readings history list for that sensor.
 *
 * Posting a new reading also updates the parent Sensor's currentValue field
 * to keep the data consistent across the API.
 *
 * Example JSON representation:
 * {
 *   "id": "a3f7c2d1-...",
 *   "timestamp": 1713000000000,
 *   "value": 23.7
 * }
 */
public class SensorReading {

    // Unique ID for this reading event. We use UUID to guarantee uniqueness.
    private String id;

    // Epoch time in milliseconds when the reading was captured.
    // e.g. 1713000000000 = a specific point in time
    private long timestamp;

    // The actual measured value, e.g. 23.7 degrees, 450 ppm CO2, 12 people
    private double value;

    // No-argument constructor required by Jackson for JSON deserialisation
    public SensorReading() {}

    // Convenience constructor that auto-generates ID and timestamp
    public SensorReading(double value) {
        this.id = UUID.randomUUID().toString(); // generates a unique ID automatically
        this.timestamp = System.currentTimeMillis(); // current time in milliseconds
        this.value = value;
    }

    // Getters and setters - required by Jackson to read/write each field as JSON
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
}
