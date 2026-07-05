package com.example.todolist.service.impl;

import com.example.todolist.dto.PasswordResetRequestDTO;
import com.example.todolist.dto.UserCreateRequestDTO;
import com.example.todolist.dto.UserResponseDTO;
import com.example.todolist.dto.UserUpdateRequestDTO;
import com.example.todolist.exception.DuplicateUsernameException;
import com.example.todolist.exception.UserNotFoundException;
import com.example.todolist.model.Role;
import com.example.todolist.model.User;
import com.example.todolist.repository.NotificationRepository;
import com.example.todolist.repository.TodoRepository;
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
    private final TodoRepository todoRepository;
    private final NotificationRepository notificationRepository;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public UserResponseDTO updateEmployeeProfile(Long id, UserUpdateRequestDTO requestDTO) {
        User employee = findEmployeeOrThrow(id);
        employee.setFullName(requestDTO.getFullName());
        employee.setPosition(requestDTO.getPosition());
        User saved = userRepository.save(employee);
        return UserResponseDTO.from(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetPassword(Long id, PasswordResetRequestDTO requestDTO) {
        User employee = findEmployeeOrThrow(id);
        employee.setPassword(passwordEncoder.encode(requestDTO.getNewPassword()));
        userRepository.save(employee);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String removeEmployee(Long id, boolean force) {
        User employee = findEmployeeOrThrow(id);

        if (force) {
            if (employee.isActive()) {
                throw new IllegalArgumentException(
                        "Chỉ có thể xóa hẳn tài khoản đã bị vô hiệu hóa (nghỉ việc) — hãy xóa lần đầu để vô hiệu hóa trước");
            }
            notificationRepository.clearActor(employee);
            todoRepository.deleteByAssignee(employee);
            userRepository.delete(employee);
            return "Đã xóa hẳn tài khoản nhân viên và toàn bộ lịch sử công việc liên quan";
        }

        if (!todoRepository.existsByAssignee(employee)) {
            userRepository.delete(employee);
            return "Đã xóa tài khoản nhân viên";
        }

        employee.setActive(false);
        userRepository.save(employee);
        return "Nhân viên đã có lịch sử công việc nên được vô hiệu hóa thay vì xóa hẳn "
                + "(giữ lại số liệu năng suất, không thể đăng nhập hoặc được giao việc mới)";
    }

    private User findEmployeeOrThrow(Long id) {
        return userRepository.findById(id)
                .filter(user -> user.getRole() == Role.EMPLOYEE)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy nhân viên với id: " + id));
    }
}
