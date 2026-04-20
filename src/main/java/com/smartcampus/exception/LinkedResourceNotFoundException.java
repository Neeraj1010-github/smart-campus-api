package com.smartcampus.exception;

/**
 * LinkedResourceNotFoundException.java - Part 5.2: Custom exception for invalid references.
 *
 * Thrown when a client tries to POST a new Sensor with a roomId that
 * does not exist in the system.
 *
 * This exception is caught by LinkedResourceNotFoundExceptionMapper which
 * converts it to an HTTP 422 Unprocessable Entity response.
 *
 * WHY 422 and not 404?
 * 404 = the URL/endpoint itself was not found.
 * 422 = the URL is valid and the JSON is well-formed, but a value INSIDE
 *       the JSON body (the roomId) references something that doesn't exist.
 * 422 is more semantically accurate for this situation.
 */
public class LinkedResourceNotFoundException extends RuntimeException {

    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}
