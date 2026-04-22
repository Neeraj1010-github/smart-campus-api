package com.smartcampus.exception;

/**
 * SensorUnavailableException.java - Part 5.3: Custom exception for maintenance state.
 *
 * Thrown when a client tries to POST a new reading to a sensor
 * whose status is "MAINTENANCE".
 *
 * A sensor in MAINTENANCE is physically disconnected and cannot
 * send or receive data. This exception is caught by
 * SensorUnavailableExceptionMapper which returns HTTP 403 Forbidden.
 */
public class SensorUnavailableException extends RuntimeException {

    public SensorUnavailableException(String message) {
        super(message);
    }
}
