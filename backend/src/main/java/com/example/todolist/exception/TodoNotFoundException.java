package com.example.todolist.exception;

/**
 * Thrown when a requested Todo item cannot be found by its identifier.
 */
public class TodoNotFoundException extends RuntimeException {

    public TodoNotFoundException(String message) {
        super(message);
    }
}
