package com.smartcampus.application;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * SmartCampusApplication.java - The JAX-RS application entry point.
 *
 * The @ApplicationPath("/api/v1") annotation tells JAX-RS (Jersey) that
 * every endpoint in this application should be prefixed with "/api/v1".
 *
 * So if a resource class has @Path("/rooms"), the full URL becomes:
 *   http://localhost:8080/smart-campus-api/api/v1/rooms
 *
 * With Tomcat, we don't need a Main.java. Tomcat acts as the server and
 * automatically finds this class because it extends Application and has
 * the @ApplicationPath annotation. Jersey then scans for all @Path,
 * @Provider etc. annotated classes automatically.
 *
 * JAX-RS LIFECYCLE:
 * By default, JAX-RS creates a NEW instance of each resource class
 * for every incoming HTTP request (per-request lifecycle).
 * This is why we use the singleton DataStore - data stored as a field
 * inside a resource class would be lost after every single request.
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {
    
    // via the @ApplicationPath annotation and classpath scanning.
}
