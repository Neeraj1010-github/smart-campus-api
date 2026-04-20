package com.smartcampus.resource;

import com.smartcampus.application.DataStore;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Sensor;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SensorResource.java - Part 3 & 4: Sensor Operations and Sub-Resource Locator.
 *
 * Handles all HTTP requests under /api/v1/sensors
 *
 * Endpoints provided:
 *   GET  /api/v1/sensors              - list all sensors (with optional ?type= filter)
 *   POST /api/v1/sensors              - register a new sensor
 *   GET  /api/v1/sensors/{sensorId}   - get one sensor by ID
 *
 * Sub-resource locator (Part 4):
 *   ANY  /api/v1/sensors/{sensorId}/readings - delegates to SensorReadingResource
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    // Get the shared singleton data store
    private final DataStore store = DataStore.getInstance();

    /**
     * GET /api/v1/sensors
     * GET /api/v1/sensors?type=CO2
     *
     * Returns all sensors, optionally filtered by type.
     *
     * @QueryParam("type") binds the ?type=xxx URL parameter to the 'type' variable.
     * If no ?type= is provided, 'type' will be null and all sensors are returned.
     *
     * Using @QueryParam for filtering (rather than a path like /sensors/type/CO2)
     * is better because query params are designed for optional filtering of collections.
     * They also compose easily: ?type=CO2&status=ACTIVE
     */
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        Collection<Sensor> sensors = store.getSensors().values();

        // If a type filter was provided, filter the list using Java streams
        if (type != null && !type.isBlank()) {
            List<Sensor> filtered = sensors.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type)) // case-insensitive match
                    .collect(Collectors.toList());
            return Response.ok(filtered).build();
        }

        // No filter - return everything
        return Response.ok(sensors).build();
    }

    /**
     * POST /api/v1/sensors
     * Registers a new sensor in the system.
     *
     * VALIDATION (Part 3.1):
     * The roomId in the request body must reference an existing room.
     * If not, we throw LinkedResourceNotFoundException -> HTTP 422.
     *
     * @Consumes(APPLICATION_JSON) means JAX-RS will reject any request that
     * doesn't have Content-Type: application/json, returning HTTP 415 automatically.
     */
    @POST
    public Response createSensor(Sensor sensor) {

        // Basic validation - sensor must have an ID
        if (sensor == null || sensor.getId() == null || sensor.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Sensor ID is required."))
                    .build();
        }

        // REFERENTIAL INTEGRITY CHECK: the roomId must exist in the system
        // If it doesn't, throw an exception that maps to HTTP 422 Unprocessable Entity
        if (sensor.getRoomId() == null || !store.getRooms().containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                    "The roomId '" + sensor.getRoomId() + "' does not exist. " +
                    "Please link the sensor to a valid room."
            );
        }

        // Check for duplicate sensor ID
        if (store.getSensors().containsKey(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", "A sensor with ID '" + sensor.getId() + "' already exists."))
                    .build();
        }

        // Default status to ACTIVE if not specified
        if (sensor.getStatus() == null || sensor.getStatus().isBlank()) {
            sensor.setStatus("ACTIVE");
        }

        // Save the sensor
        store.getSensors().put(sensor.getId(), sensor);

        // Add this sensor's ID to its room's sensorIds list (bidirectional link)
        store.getRooms().get(sensor.getRoomId()).getSensorIds().add(sensor.getId());

        // Initialise an empty readings history list for this new sensor
        store.getSensorReadings().put(sensor.getId(), new ArrayList<>());

        // Success response with HATEOAS links
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Sensor registered successfully.");
        response.put("sensor", sensor);
        response.put("_links", Map.of(
            "self",     "/api/v1/sensors/" + sensor.getId(),
            "readings", "/api/v1/sensors/" + sensor.getId() + "/readings"
        ));
        return Response.status(Response.Status.CREATED).entity(response).build(); // HTTP 201
    }

    /**
     * GET /api/v1/sensors/{sensorId}
     * Returns a single sensor by ID.
     */
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Sensor with ID '" + sensorId + "' not found."))
                    .build();
        }
        return Response.ok(sensor).build();
    }

    /**
     * SUB-RESOURCE LOCATOR - Part 4.1
     *
     * This method handles any request to /api/v1/sensors/{sensorId}/readings
     * It does NOT have a @GET, @POST etc. annotation - that's what makes it a
     * "sub-resource locator" rather than a regular endpoint.
     *
     * Instead of handling the request itself, it creates and returns an instance
     * of SensorReadingResource, which then handles the request.
     *
     * BENEFIT: Keeps this class focused on sensor CRUD only.
     * SensorReadingResource handles all reading logic independently.
     * This is the Single Responsibility Principle in action.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {

        // First validate the sensor exists before delegating
        if (!store.getSensors().containsKey(sensorId)) {
            // NotFoundException is a built-in JAX-RS exception that returns HTTP 404
            throw new NotFoundException("Sensor with ID '" + sensorId + "' not found.");
        }

        // Return a new SensorReadingResource instance for this specific sensor
        // JAX-RS will then route the request to the appropriate method in that class
        return new SensorReadingResource(sensorId);
    }
}
