package com.example.todolist.exception;

/**
 * Thrown when attempting to create a user account with a username that already exists.
 */
public class DuplicateUsernameException extends AppException {

    public DuplicateUsernameException(String message) {
        super(message);
    }
}
