package com.smartcampus.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * RoomNotEmptyExceptionMapper.java - Part 5.1: Maps RoomNotEmptyException to HTTP 409.
 */
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    private static final Logger LOGGER = Logger.getLogger(RoomNotEmptyExceptionMapper.class.getName());

    @Override
    public Response toResponse(RoomNotEmptyException exception) {

        // Log the issue server-side for debugging
        LOGGER.warning("409 Conflict - Room not empty: " + exception.getMessage());

        // Build a clean JSON error response (no stack trace exposed to client)
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("status",  409);
        error.put("error",   "Conflict");
        error.put("message", exception.getMessage());
        error.put("hint",    "Remove or reassign all sensors from the room before decommissioning it.");

        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
