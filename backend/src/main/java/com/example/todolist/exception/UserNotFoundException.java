package com.example.todolist.exception;

/**
 * Thrown when a requested user account cannot be found.
 */
public class UserNotFoundException extends ResourceNotFoundException {

    public UserNotFoundException(String message) {
        super(message);
    }
}
