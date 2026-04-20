package com.smartcampus.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * SensorUnavailableExceptionMapper.java - Part 5.3: Maps to HTTP 403 Forbidden.
 *
 * Triggered when posting a reading to a sensor in MAINTENANCE status.
 * Returns HTTP 403 Forbidden with a descriptive JSON error body.
 */
@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {

    private static final Logger LOGGER = Logger.getLogger(SensorUnavailableExceptionMapper.class.getName());

    @Override
    public Response toResponse(SensorUnavailableException exception) {

        LOGGER.warning("403 Forbidden - Sensor unavailable: " + exception.getMessage());

        Map<String, Object> error = new LinkedHashMap<>();
        error.put("status",  403);
        error.put("error",   "Forbidden");
        error.put("message", exception.getMessage());
        error.put("hint",    "Sensor must be ACTIVE or OFFLINE to accept new readings.");

        return Response.status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
