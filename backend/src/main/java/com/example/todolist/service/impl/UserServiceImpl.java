package com.example.todolist.service.impl;

import com.example.todolist.dto.UserCreateRequestDTO;
import com.example.todolist.dto.UserResponseDTO;
import com.example.todolist.exception.DuplicateUsernameException;
import com.example.todolist.model.Role;
import com.example.todolist.model.User;
import com.example.todolist.repository.UserRepository;
import com.example.todolist.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default implementation of {@link UserService}.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * {@inheritDoc}
     */
    @Override
    public UserResponseDTO createEmployee(UserCreateRequestDTO requestDTO) {
        if (userRepository.existsByUsername(requestDTO.getUsername())) {
            throw new DuplicateUsernameException("Tên đăng nhập đã tồn tại: " + requestDTO.getUsername());
        }

        User employee = User.builder()
                .username(requestDTO.getUsername())
                .password(passwordEncoder.encode(requestDTO.getPassword()))
                .fullName(requestDTO.getFullName())
                .position(requestDTO.getPosition())
                .role(Role.EMPLOYEE)
                .build();

        User saved = userRepository.save(employee);
        return UserResponseDTO.from(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> listEmployees() {
        return userRepository.findByRole(Role.EMPLOYEE).stream()
                .map(UserResponseDTO::from)
                .toList();
    }
}
