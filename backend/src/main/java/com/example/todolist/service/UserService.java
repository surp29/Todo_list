package com.example.todolist.service;

import com.example.todolist.dto.PasswordResetRequestDTO;
import com.example.todolist.dto.UserCreateRequestDTO;
import com.example.todolist.dto.UserResponseDTO;
import com.example.todolist.dto.UserUpdateRequestDTO;

import java.util.List;

/**
 * Business logic for managing user accounts.
 */
public interface UserService {

    /**
     * Creates a new Employee account. Only callable by a Leader.
     *
     * @param requestDTO the new employee's credentials and profile
     * @return the created account as a response DTO
     */
    UserResponseDTO createEmployee(UserCreateRequestDTO requestDTO);

    /**
     * Lists every Employee account (active and deactivated), used to populate
     * the employee management screen and task-assignment pickers.
     *
     * @return all employee accounts
     */
    List<UserResponseDTO> listEmployees();

    /**
     * Updates an employee's name and/or position.
     *
     * @param id         the employee's id
     * @param requestDTO the new profile data
     * @return the updated account as a response DTO
     */
    UserResponseDTO updateEmployeeProfile(Long id, UserUpdateRequestDTO requestDTO);

    /**
     * Resets an employee's password (e.g. when they forget it). Only a Leader may call this.
     *
     * @param id         the employee's id
     * @param requestDTO the new password
     */
    void resetPassword(Long id, PasswordResetRequestDTO requestDTO);

    /**
     * Removes an employee from the active roster. If the employee has no assigned
     * tasks at all, the account is permanently deleted; otherwise it is deactivated
     * (kept for historical/productivity records but can no longer log in or be
     * assigned new tasks) unless {@code force} is set.
     *
     * @param id    the employee's id
     * @param force when {@code true} on an already-deactivated employee, permanently
     *              deletes the account along with all of their assigned tasks — e.g. to
     *              free up the username for a new hire. Rejected if the employee is still
     *              active, to make sure task history is never lost by accident.
     * @return a human-readable message describing which action was taken
     */
    String removeEmployee(Long id, boolean force);
}
