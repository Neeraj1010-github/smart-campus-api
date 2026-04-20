package com.smartcampus.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DiscoveryResource.java - Part 1: The API Discovery Endpoint.
 *
 * Handles GET /api/v1
 *
 * This endpoint acts as the "front door" of the API. It returns metadata
 * about the API including version info, contact details, and hypermedia
 * links to the main resource collections (HATEOAS).
 *
 * HATEOAS (Hypermedia as the Engine of Application State) means we include
 * links in our responses so clients can discover what they can do next,
 * rather than having to read static documentation.
 *
 * @Path("/") maps this to the root of our API base path (/api/v1)
 * @Produces(MediaType.APPLICATION_JSON) means all responses are JSON
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    /**
     * GET /api/v1
     * Returns API metadata and navigation links.
     */
    @GET
    public Response discover() {

        // Build the response as a map - Jackson will convert this to JSON automatically
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("api",         "Smart Campus Sensor & Room Management API");
        response.put("version",     "1.0.0");
        response.put("description", "RESTful API for managing campus rooms and IoT sensors.");
        response.put("contact",     "admin@smartcampus.ac.uk");
        response.put("status",      "operational");

        // HATEOAS links - tells clients where to find the main resources
        // "_links" is a common convention for hypermedia links in REST APIs
        Map<String, String> links = new LinkedHashMap<>();
        links.put("self",    "/api/v1");
        links.put("rooms",   "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        response.put("_links", links);

        // Response.ok() = HTTP 200 OK
        return Response.ok(response).build();
    }
}
