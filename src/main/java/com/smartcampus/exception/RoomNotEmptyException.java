package com.smartcampus.exception;

/**
 * RoomNotEmptyException.java - Part 5.1: Custom exception for room deletion conflict.
 *
 * Thrown when a client tries to DELETE a room that still has sensors assigned to it.
 * This prevents data orphans (sensors referencing a non-existent room).
 *
 * This exception is caught by RoomNotEmptyExceptionMapper which converts it
 * to an HTTP 409 Conflict response with a descriptive JSON error body.
 *
 * We extend RuntimeException so we don't need to declare it in the method signatures.
 
 */
public class RoomNotEmptyException extends RuntimeException {

    public RoomNotEmptyException(String message) {
        super(message); // pass the message up to RuntimeException
    }
}
