package com.example.todolist.service;

import com.example.todolist.dto.PageResponseDTO;
import com.example.todolist.dto.TodoRequestDTO;
import com.example.todolist.dto.TodoResponseDTO;
import com.example.todolist.exception.TodoNotFoundException;
import com.example.todolist.mapper.TodoMapper;
import com.example.todolist.model.Role;
import com.example.todolist.model.Todo;
import com.example.todolist.model.TodoPriority;
import com.example.todolist.model.TodoStatus;
import com.example.todolist.model.User;
import com.example.todolist.repository.TodoRepository;
import com.example.todolist.repository.UserRepository;
import com.example.todolist.service.impl.TodoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TodoMapper todoMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TodoServiceImpl todoService;

    private User leader;
    private User employee;
    private Todo todo;
    private TodoRequestDTO requestDTO;
    private TodoResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        leader = User.builder().id(1L).username("leader").fullName("Truong nhom").role(Role.LEADER).build();
        employee = User.builder().id(2L).username("nhanvien").fullName("Nguyen Van A").role(Role.EMPLOYEE).build();

        todo = Todo.builder()
                .id(1L)
                .title("Học Spring Boot")
                .description("Ôn tập cho bài test")
                .status(TodoStatus.PENDING)
                .priority(TodoPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(3))
                .assignee(employee)
                .createdBy(leader)
                .build();

        requestDTO = TodoRequestDTO.builder()
                .title("Học Spring Boot")
                .description("Ôn tập cho bài test")
                .status(TodoStatus.PENDING)
                .priority(TodoPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(3))
                .assigneeId(2L)
                .build();

        responseDTO = TodoResponseDTO.builder()
                .id(1L)
                .title("Học Spring Boot")
                .description("Ôn tập cho bài test")
                .status(TodoStatus.PENDING)
                .priority(TodoPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(3))
                .assigneeId(2L)
                .build();
    }

    @Test
    void test_createTodo_success() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(employee));
        when(todoMapper.toEntity(requestDTO)).thenReturn(todo);
        when(todoRepository.save(todo)).thenReturn(todo);
        when(todoMapper.toResponseDTO(todo)).thenReturn(responseDTO);

        TodoResponseDTO result = todoService.createTodo(requestDTO, leader);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Học Spring Boot");
        verify(todoRepository, times(1)).save(todo);
    }

    @Test
    void test_createTodo_titleBlank_throwsException() {
        TodoRequestDTO invalidRequest = TodoRequestDTO.builder()
                .title("   ")
                .status(TodoStatus.PENDING)
                .priority(TodoPriority.LOW)
                .assigneeId(2L)
                .build();

        assertThatThrownBy(() -> todoService.createTodo(invalidRequest, leader))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tiêu đề");

        verify(todoRepository, never()).save(any());
    }

    @Test
    void test_getTodoById_found() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
        when(todoMapper.toResponseDTO(todo)).thenReturn(responseDTO);

        TodoResponseDTO result = todoService.getTodoById(1L, leader);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void test_getTodoById_notFound_throwsException() {
        when(todoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> todoService.getTodoById(99L, leader))
                .isInstanceOf(TodoNotFoundException.class);
    }

    @Test
    void test_getTodoById_employeeNotOwner_throwsAccessDenied() {
        User otherEmployee = User.builder().id(3L).username("khac").fullName("Nguyen Van B").role(Role.EMPLOYEE).build();
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));

        assertThatThrownBy(() -> todoService.getTodoById(1L, otherEmployee))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void test_updateTodo_success() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
        doNothing().when(todoMapper).updateEntityFromDTO(requestDTO, todo);
        when(todoRepository.save(todo)).thenReturn(todo);
        when(todoMapper.toResponseDTO(todo)).thenReturn(responseDTO);

        TodoResponseDTO result = todoService.updateTodo(1L, requestDTO, leader);

        assertThat(result).isNotNull();
        verify(todoRepository, times(1)).save(todo);
    }

    @Test
    void test_updateTodo_notFound_throwsException() {
        when(todoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> todoService.updateTodo(99L, requestDTO, leader))
                .isInstanceOf(TodoNotFoundException.class);

        verify(todoRepository, never()).save(any());
    }

    @Test
    void test_deleteTodo_success() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));

        todoService.deleteTodo(1L, leader);

        verify(todoRepository, times(1)).delete(todo);
    }

    @Test
    void test_deleteTodo_notFound_throwsException() {
        when(todoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> todoService.deleteTodo(99L, leader))
                .isInstanceOf(TodoNotFoundException.class);

        verify(todoRepository, never()).delete(any(Todo.class));
    }

    @Test
    void test_getAllTodos_withFilter_returnsPaginatedResult() {
        List<Todo> todos = List.of(todo);
        Page<Todo> page = new PageImpl<>(todos, PageRequest.of(0, 10), 1);

        when(todoRepository.findWithFilters(
                eq("%spring%"), eq(TodoStatus.PENDING), eq(TodoPriority.HIGH), isNull(), any()))
                .thenReturn(page);
        when(todoMapper.toResponseDTOList(todos)).thenReturn(List.of(responseDTO));

        PageResponseDTO<TodoResponseDTO> result = todoService.getAllTodos(
                "spring", TodoStatus.PENDING, TodoPriority.HIGH, null, 0, 10, "createdAt", "DESC", leader);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.isFirst()).isTrue();
    }

    @Test
    void test_getAllTodos_asEmployee_forcesOwnAssigneeId() {
        List<Todo> todos = List.of(todo);
        Page<Todo> page = new PageImpl<>(todos, PageRequest.of(0, 10), 1);

        when(todoRepository.findWithFilters(isNull(), isNull(), isNull(), eq(2L), any()))
                .thenReturn(page);
        when(todoMapper.toResponseDTOList(todos)).thenReturn(List.of(responseDTO));

        // Employee requests assigneeId=999 (someone else) but the service must force it to their own id (2L).
        todoService.getAllTodos(null, null, null, 999L, 0, 10, "createdAt", "DESC", employee);

        verify(todoRepository).findWithFilters(isNull(), isNull(), isNull(), eq(2L), any());
    }

    @Test
    void test_toggleStatus_pendingToCompleted_notifiesLeader() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
        when(todoRepository.save(any(Todo.class))).thenReturn(todo);

        TodoResponseDTO completedResponse = TodoResponseDTO.builder()
                .id(1L)
                .title(todo.getTitle())
                .status(TodoStatus.COMPLETED)
                .priority(todo.getPriority())
                .build();
        when(todoMapper.toResponseDTO(any(Todo.class))).thenReturn(completedResponse);

        TodoResponseDTO result = todoService.updateStatus(1L, TodoStatus.COMPLETED, employee);

        assertThat(result.getStatus()).isEqualTo(TodoStatus.COMPLETED);
        assertThat(todo.getStatus()).isEqualTo(TodoStatus.COMPLETED);
        verify(notificationService, times(1)).notifyTaskCompleted(todo);
    }

    @Test
    void test_updateStatus_employeeNotOwner_throwsAccessDenied() {
        User otherEmployee = User.builder().id(3L).username("khac").fullName("Nguyen Van B").role(Role.EMPLOYEE).build();
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));

        assertThatThrownBy(() -> todoService.updateStatus(1L, TodoStatus.COMPLETED, otherEmployee))
                .isInstanceOf(AccessDeniedException.class);

        verify(todoRepository, never()).save(any());
        verify(notificationService, never()).notifyTaskCompleted(any());
    }
}
