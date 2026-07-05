package com.example.todolist.exception;

/**
 * Root of this application's custom exception hierarchy, allowing callers
 * (e.g. {@link GlobalExceptionHandler}) to reason about "our" exceptions as
 * a single family distinct from generic {@link RuntimeException}s.
 */
public abstract class AppException extends RuntimeException {

    protected AppException(String message) {
        super(message);
    }
}
