package com.example.todolist.exception;

/**
 * Thrown when any domain resource cannot be found by its identifier, letting
 * {@link GlobalExceptionHandler} map the whole family to a single 404 response.
 */
public abstract class ResourceNotFoundException extends AppException {

    protected ResourceNotFoundException(String message) {
        super(message);
    }
}
