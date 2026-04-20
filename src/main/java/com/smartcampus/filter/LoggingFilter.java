package com.smartcampus.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * LoggingFilter.java - Part 5.5: API Request and Response Logging.
 *
 * This single class implements BOTH:
 *   - ContainerRequestFilter  : runs BEFORE every request reaches a resource method
 *   - ContainerResponseFilter : runs AFTER every response leaves a resource method
 *
 * @Provider tells JAX-RS to automatically register this as a filter.
 *
 * WHY USE A FILTER instead of putting Logger.info() in every resource method?
 *
 * 1. No code duplication: one filter handles logging for ALL endpoints automatically.
 *    Adding Logger.info() to every method violates DRY (Don't Repeat Yourself).
 *
 * 2. Separation of concerns: resource methods should only contain business logic.
 *    Logging is a cross-cutting infrastructure concern - it belongs in a filter.
 *
 * 3. Consistency: a filter guarantees every single request/response is logged.
 *    Manual logging is easy to forget when adding new endpoints.
 *
 * 4. Easy to extend: to change log format or add request IDs, update ONE file,
 *    not every resource class across the codebase.
 *
 * Example log output:
 *   [REQUEST]  --> POST http://localhost:8080/api/v1/rooms
 *   [RESPONSE] <-- POST http://localhost:8080/api/v1/rooms | Status: 201
 */
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

    /**
     * Called BEFORE the request reaches the resource method.
     * Logs the HTTP method and full URI of every incoming request.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String method = requestContext.getMethod(); // e.g. "GET", "POST"
        String uri    = requestContext.getUriInfo().getRequestUri().toString();
        LOGGER.info(String.format("[REQUEST]  --> %s %s", method, uri));
    }

    /**
     * Called AFTER the resource method produces a response.
     * Logs the HTTP method, URI, and final status code of every outgoing response.
     */
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        String method = requestContext.getMethod();
        String uri    = requestContext.getUriInfo().getRequestUri().toString();
        int    status = responseContext.getStatus(); // e.g. 200, 201, 404, 409
        LOGGER.info(String.format("[RESPONSE] <-- %s %s | Status: %d", method, uri, status));
    }
}
