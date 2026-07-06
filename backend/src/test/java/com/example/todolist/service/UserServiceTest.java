package com.example.todolist.service;

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
import com.example.todolist.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User employee;

    @BeforeEach
    void setUp() {
        employee = User.builder()
                .id(2L)
                .username("nhanvien")
                .password("hashed-old")
                .fullName("Nguyen Van A")
                .position("Developer")
                .role(Role.EMPLOYEE)
                .active(true)
                .build();
    }

    @Test
    void test_createEmployee_success() {
        UserCreateRequestDTO requestDTO = UserCreateRequestDTO.builder()
                .username("nhanvien2")
                .password("matkhau123")
                .fullName("Nguyen Van B")
                .position("Tester")
                .build();
        when(userRepository.existsByUsername("nhanvien2")).thenReturn(false);
        when(passwordEncoder.encode("matkhau123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponseDTO result = userService.createEmployee(requestDTO);

        assertThat(result.getFullName()).isEqualTo("Nguyen Van B");
        assertThat(result.getPosition()).isEqualTo("Tester");
    }

    @Test
    void test_createEmployee_duplicateUsername_throwsException() {
        UserCreateRequestDTO requestDTO = UserCreateRequestDTO.builder()
                .username("nhanvien")
                .password("matkhau123")
                .fullName("Nguyen Van B")
                .build();
        when(userRepository.existsByUsername("nhanvien")).thenReturn(true);

        assertThatThrownBy(() -> userService.createEmployee(requestDTO))
                .isInstanceOf(DuplicateUsernameException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void test_updateEmployeeProfile_success() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(employee));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserUpdateRequestDTO requestDTO = UserUpdateRequestDTO.builder()
                .fullName("Nguyen Van A2")
                .position("Senior Developer")
                .build();

        UserResponseDTO result = userService.updateEmployeeProfile(2L, requestDTO);

        assertThat(result.getFullName()).isEqualTo("Nguyen Van A2");
        assertThat(result.getPosition()).isEqualTo("Senior Developer");
    }

    @Test
    void test_updateEmployeeProfile_notFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateEmployeeProfile(99L, UserUpdateRequestDTO.builder().fullName("X").build()))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void test_resetPassword_encodesAndSaves() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(employee));
        when(passwordEncoder.encode("matkhaumoi")).thenReturn("hashed-new");

        userService.resetPassword(2L, new PasswordResetRequestDTO("matkhaumoi"));

        assertThat(employee.getPassword()).isEqualTo("hashed-new");
        verify(userRepository, times(1)).save(employee);
    }

    @Test
    void test_removeEmployee_noAssignedTasks_hardDeletes() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(employee));
        when(todoRepository.existsByAssignee(employee)).thenReturn(false);

        String message = userService.removeEmployee(2L, false);

        verify(userRepository, times(1)).delete(employee);
        verify(userRepository, never()).save(any());
        assertThat(message).containsIgnoringCase("xóa");
    }

    @Test
    void test_removeEmployee_hasAssignedTasks_deactivatesInstead() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(employee));
        when(todoRepository.existsByAssignee(employee)).thenReturn(true);

        String message = userService.removeEmployee(2L, false);

        assertThat(employee.isActive()).isFalse();
        verify(userRepository, times(1)).save(employee);
        verify(userRepository, never()).delete(any());
        assertThat(message).containsIgnoringCase("vô hiệu hóa");
    }

    @Test
    void test_removeEmployee_onlyOrphanedTaskAssignedNotification_deactivatesInstead() {
        // No todos currently assigned (e.g. the leader deleted the task after assigning it),
        // but the employee still has a "you were assigned task X" notification as recipient.
        when(userRepository.findById(2L)).thenReturn(Optional.of(employee));
        when(todoRepository.existsByAssignee(employee)).thenReturn(false);
        when(notificationRepository.existsByRecipient(employee)).thenReturn(true);

        String message = userService.removeEmployee(2L, false);

        assertThat(employee.isActive()).isFalse();
        verify(userRepository, times(1)).save(employee);
        verify(userRepository, never()).delete(any());
        assertThat(message).containsIgnoringCase("vô hiệu hóa");
    }

    @Test
    void test_removeEmployee_forceOnDeactivatedAccount_permanentlyDeletesEverything() {
        employee.setActive(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(employee));

        String message = userService.removeEmployee(2L, true);

        verify(notificationRepository, times(1)).clearActor(employee);
        verify(notificationRepository, times(1)).deleteByRecipient(employee);
        verify(todoRepository, times(1)).deleteByAssignee(employee);
        verify(userRepository, times(1)).delete(employee);
        assertThat(message).containsIgnoringCase("xóa hẳn");
    }

    @Test
    void test_removeEmployee_forceOnStillActiveAccount_throwsException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(employee)); // still active by default

        assertThatThrownBy(() -> userService.removeEmployee(2L, true))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository, never()).delete(any());
        verify(todoRepository, never()).deleteByAssignee(any());
    }
}
