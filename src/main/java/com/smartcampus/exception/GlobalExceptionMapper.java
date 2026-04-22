package com.smartcampus.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GlobalExceptionMapper.java - Part 5.4: The "catch-all" safety net.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {

        // Log the FULL stack trace on the server (developers can see it in the console)
        // but NEVER send it to the client
        LOGGER.log(Level.SEVERE,
                "Unhandled exception caught by global safety net: " + exception.getMessage(),
                exception);

        // Return a safe, generic error message to the client
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("status",  500);
        error.put("error",   "Internal Server Error");
        error.put("message", "An unexpected error occurred. Please try again later or contact support.");
        // Note: no stack trace, no internal details - nothing useful to an attacker

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
