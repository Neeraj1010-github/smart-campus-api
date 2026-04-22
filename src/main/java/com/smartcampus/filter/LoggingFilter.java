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
