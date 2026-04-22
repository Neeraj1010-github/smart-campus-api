package com.smartcampus.resource;

import com.smartcampus.application.DataStore;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.*;

/**
 * RoomResource.java - Part 2: Room Management.
 
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    // Get the shared singleton data store
    private final DataStore store = DataStore.getInstance();

    /**
     * GET /api/v1/rooms
     * Returns a list of all rooms in the system.
     */
    @GET
    public Response getAllRooms() {
        // .values() gives us all Room objects from the map
        Collection<Room> rooms = store.getRooms().values();
        return Response.ok(rooms).build(); // HTTP 200 OK
    }

    /**
     * POST /api/v1/rooms
     * Creates a new room. JAX-RS automatically deserialises the JSON request
     * body into a Room object using Jackson.
     */
    @POST
    public Response createRoom(Room room) {

        // Validate that an ID was provided
        if (room == null || room.getId() == null || room.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorBody("Room ID is required."))
                    .build(); // HTTP 400 Bad Request
        }

        // Check for duplicate ID - can't have two rooms with the same ID
        if (store.getRooms().containsKey(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(errorBody("A room with ID '" + room.getId() + "' already exists."))
                    .build(); // HTTP 409 Conflict
        }

        // Store the new room
        store.getRooms().put(room.getId(), room);

        // Build a descriptive success response with a HATEOAS link
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Room created successfully.");
        response.put("room", room);
        response.put("_links", Map.of("self", "/api/v1/rooms/" + room.getId()));

        return Response.status(Response.Status.CREATED).entity(response).build(); // HTTP 201 Created
    }

    /**
     * GET /api/v1/rooms/{roomId}
     * Returns a single room by its ID.
     * @PathParam("roomId") extracts the {roomId} part from the URL.
     */
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);

        // Return 404 if the room doesn't exist
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody("Room with ID '" + roomId + "' not found."))
                    .build(); // HTTP 404 Not Found
        }

        return Response.ok(room).build(); // HTTP 200 OK
    }

    /**
     * DELETE /api/v1/rooms/{roomId}
     * Decommissions (deletes) a room.
     *
     * BUSINESS LOGIC CONSTRAINT (Part 2.2):
     * A room cannot be deleted if it still has sensors assigned to it.
     * This prevents "orphaned" sensors that reference a non-existent room.
     * If sensors are present, we throw RoomNotEmptyException which is
     * caught by RoomNotEmptyExceptionMapper and returned as HTTP 409.
     *
     * IDEMPOTENCY:
     * DELETE is idempotent - calling it multiple times has the same end result.
     * First call: room exists -> deleted -> 200 OK
     * Second call: room already gone -> 404 Not Found
     * The server state (room is absent) is the same after both calls.
     */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);

        // Room doesn't exist - return 404
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody("Room with ID '" + roomId + "' not found."))
                    .build();
        }

        // SAFETY CHECK: block deletion if sensors are still assigned to this room
        if (!room.getSensorIds().isEmpty()) {
            // This exception is caught by RoomNotEmptyExceptionMapper -> HTTP 409
            throw new RoomNotEmptyException(
                    "Cannot decommission room '" + roomId + "'. It still has " +
                    room.getSensorIds().size() + " sensor(s) assigned: " +
                    room.getSensorIds()
            );
        }

        // Safe to delete
        store.getRooms().remove(roomId);

        Map<String, String> response = new LinkedHashMap<>();
        response.put("message", "Room '" + roomId + "' has been successfully decommissioned.");
        return Response.ok(response).build(); // HTTP 200 OK
    }

    /**
     * Helper method to build a simple error response body.
     * Returns a Map which Jackson converts to: {"error": "message here"}
     */
    private Map<String, String> errorBody(String message) {
        return Map.of("error", message);
    }
}
