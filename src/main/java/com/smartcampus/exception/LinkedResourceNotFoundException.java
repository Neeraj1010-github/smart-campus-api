package com.smartcampus.exception;

/**
 * LinkedResourceNotFoundException.java - Part 5.2: Custom exception for invalid references.
 */
public class LinkedResourceNotFoundException extends RuntimeException {

    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}
