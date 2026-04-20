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
 *
 * Implements ExceptionMapper<Throwable> - this catches ANY exception that
 * isn't already handled by the more specific mappers above (RoomNotEmpty,
 * LinkedResourceNotFound, SensorUnavailable).
 *
 * This includes unexpected runtime errors like:
 *   - NullPointerException
 *   - IndexOutOfBoundsException
 *   - Any other unhandled exception
 *
 * WITHOUT this mapper, those errors would return a raw Java stack trace
 * to the client, which is a serious security risk:
 *
 * SECURITY RISKS of exposing stack traces:
 * 1. Technology fingerprinting: reveals exact framework versions (Jersey 2.41,
 *    Java 11) so attackers can look up known vulnerabilities for those versions.
 * 2. Internal path disclosure: shows full package names and file structure,
 *    helping attackers map the application's internals.
 * 3. Business logic exposure: line numbers and method names reveal how the
 *    application works internally, making it easier to find exploits.
 * 4. Dependency enumeration: third-party library names let attackers search
 *    vulnerability databases (CVE, NVD) for known exploits.
 *
 * WITH this mapper: the full stack trace is logged SERVER-SIDE only (for developers),
 * while the client receives a safe, generic "something went wrong" message.
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
