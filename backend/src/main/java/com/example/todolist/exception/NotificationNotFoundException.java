package com.example.todolist.exception;

/**
 * Thrown when a requested notification cannot be found for the given recipient.
 */
public class NotificationNotFoundException extends ResourceNotFoundException {

    public NotificationNotFoundException(String message) {
        super(message);
    }
}
