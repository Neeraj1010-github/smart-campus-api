package com.smartcampus.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * LinkedResourceNotFoundExceptionMapper.java - Part 5.2: Maps to HTTP 422.
 *
 * Triggered when a sensor is registered with a roomId that doesn't exist.
 * Returns HTTP 422 Unprocessable Entity with a descriptive JSON error body.
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    private static final Logger LOGGER = Logger.getLogger(LinkedResourceNotFoundExceptionMapper.class.getName());

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {

        LOGGER.warning("422 Unprocessable Entity - Linked resource not found: " + exception.getMessage());

        Map<String, Object> error = new LinkedHashMap<>();
        error.put("status",  422);
        error.put("error",   "Unprocessable Entity");
        error.put("message", exception.getMessage());
        error.put("hint",    "Ensure the referenced roomId exists before registering a sensor.");

        // HTTP 422 - standard JAX-RS doesn't have a constant for 422 so we use the int directly
        return Response.status(422)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
