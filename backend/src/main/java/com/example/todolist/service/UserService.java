package com.example.todolist.service;

import com.example.todolist.dto.UserCreateRequestDTO;
import com.example.todolist.dto.UserResponseDTO;

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
     * Lists every Employee account, used to populate task-assignment pickers.
     *
     * @return all employee accounts
     */
    List<UserResponseDTO> listEmployees();
}
