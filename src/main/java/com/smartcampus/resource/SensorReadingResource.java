package com.smartcampus.resource;

import com.smartcampus.application.DataStore;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.*;

/**
 * SensorReadingResource.java - Part 4.2: Historical Sensor Reading Management.
 *
 * This class is NOT registered directly with JAX-RS.
 * It is instantiated by the sub-resource locator in SensorResource
 * when a request comes in for /api/v1/sensors/{sensorId}/readings
 *
 * Notice: this class has NO @Path annotation at the class level.
 * The path context (/api/v1/sensors/{sensorId}/readings) is inherited
 * from the sub-resource locator that created this instance.
 *
 * Endpoints provided (relative to /api/v1/sensors/{sensorId}/readings):
 *   GET  /     - fetch all historical readings for this sensor
 *   POST /     - add a new reading (blocked if sensor is in MAINTENANCE)
 *   GET  /{id} - fetch a specific reading by its ID
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    // The sensor ID this sub-resource is operating on.
    // Set by the constructor when SensorResource creates this instance.
    private final String sensorId;

    // Get the shared singleton data store
    private final DataStore store = DataStore.getInstance();

    /**
     * Constructor called by SensorResource's sub-resource locator.
     * @param sensorId the ID of the sensor whose readings we are managing
     */
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * GET /api/v1/sensors/{sensorId}/readings
     * Returns the full reading history for this sensor.
     */
    @GET
    public Response getReadings() {
        // getOrDefault returns an empty list if no readings exist yet (avoids null)
        List<SensorReading> readings = store.getSensorReadings()
                .getOrDefault(sensorId, Collections.emptyList());

        // Wrap in a response object with some useful metadata
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("sensorId", sensorId);
        response.put("count",    readings.size());
        response.put("readings", readings);
        return Response.ok(response).build();
    }

    /**
     * POST /api/v1/sensors/{sensorId}/readings
     * Records a new measurement reading for this sensor.
     *
     * STATE CONSTRAINT (Part 5.3):
     * If the sensor status is "MAINTENANCE", readings are rejected with HTTP 403.
     * A sensor in maintenance is physically disconnected and cannot send data.
     *
     * SIDE EFFECT (Part 4.2):
     * After saving the reading, we update the parent sensor's currentValue field
     * so it always reflects the most recent measurement. This keeps data consistent
     * across the API - GET /sensors/{id} will always show the latest value.
     */
    @POST
    public Response addReading(SensorReading reading) {

        // Null check first - reject empty request bodies
        if (reading == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Reading body is required."))
                    .build();
        }

        // Get the parent sensor object
        Sensor sensor = store.getSensors().get(sensorId);

        // MAINTENANCE CHECK: block new readings if sensor is under maintenance
        // SensorUnavailableException is caught by SensorUnavailableExceptionMapper -> HTTP 403
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor '" + sensorId + "' is currently in MAINTENANCE mode " +
                    "and cannot accept new readings."
            );
        }

        // Auto-generate ID if the client didn't provide one
        if (reading.getId() == null || reading.getId().isBlank()) {
            reading.setId(UUID.randomUUID().toString());
        }

        // Auto-set timestamp to now if not provided
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // Save the reading to the history list for this sensor
        // computeIfAbsent creates a new list if one doesn't exist yet
        store.getSensorReadings()
             .computeIfAbsent(sensorId, k -> new ArrayList<>())
             .add(reading);

        // SIDE EFFECT: update the sensor's currentValue to the new reading's value
        sensor.setCurrentValue(reading.getValue());

        // Success response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message",            "Reading recorded successfully.");
        response.put("sensorId",           sensorId);
        response.put("reading",            reading);
        response.put("updatedSensorValue", sensor.getCurrentValue());
        return Response.status(Response.Status.CREATED).entity(response).build(); // HTTP 201
    }

    /**
     * GET /api/v1/sensors/{sensorId}/readings/{readingId}
     * Returns one specific reading by its ID.
     */
    @GET
    @Path("/{readingId}")
    public Response getReadingById(@PathParam("readingId") String readingId) {
        List<SensorReading> readings = store.getSensorReadings()
                .getOrDefault(sensorId, Collections.emptyList());

        // Search through the readings list for a matching ID
        return readings.stream()
                .filter(r -> r.getId().equals(readingId))
                .findFirst()
                .map(r -> Response.ok(r).build()) // found -> 200 OK
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error",
                            "Reading '" + readingId + "' not found for sensor '" + sensorId + "'."))
                        .build()); // not found -> 404
    }
}
