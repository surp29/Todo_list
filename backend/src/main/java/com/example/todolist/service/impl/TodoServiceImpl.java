package com.example.todolist.service.impl;

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
import com.example.todolist.service.NotificationService;
import com.example.todolist.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link TodoService} containing all business logic
 * for managing Todo items, including Leader/Employee access scoping.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;
    private final TodoMapper todoMapper;
    private final NotificationService notificationService;

    /**
     * {@inheritDoc}
     */
    @Override
    public TodoResponseDTO createTodo(TodoRequestDTO requestDTO, User currentUser) {
        validateTitle(requestDTO);

        User assignee = userRepository.findById(requestDTO.getAssigneeId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy nhân viên với id: " + requestDTO.getAssigneeId()));

        Todo todo = todoMapper.toEntity(requestDTO);
        todo.setAssignee(assignee);
        todo.setCreatedBy(currentUser);

        Todo saved = todoRepository.save(todo);
        return todoMapper.toResponseDTO(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public TodoResponseDTO getTodoById(Long id, User currentUser) {
        Todo todo = findTodoOrThrow(id);
        assertCanView(todo, currentUser);
        return todoMapper.toResponseDTO(todo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<TodoResponseDTO> getAllTodos(String keyword,
                                                         TodoStatus status,
                                                         TodoPriority priority,
                                                         Long assigneeId,
                                                         int page,
                                                         int size,
                                                         String sortBy,
                                                         String sortDir,
                                                         User currentUser) {
        Sort sort = Sort.by(resolveDirection(sortDir), resolveSortProperty(sortBy));
        Pageable pageable = PageRequest.of(page, size, sort);

        String likePattern = (keyword == null || keyword.isBlank())
                ? null
                : "%" + keyword.trim().toLowerCase() + "%";

        Long effectiveAssigneeId = currentUser.getRole() == Role.EMPLOYEE ? currentUser.getId() : assigneeId;

        Page<Todo> resultPage = todoRepository.findWithFilters(
                likePattern, status, priority, effectiveAssigneeId, pageable);

        return PageResponseDTO.from(resultPage, todoMapper.toResponseDTOList(resultPage.getContent()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TodoResponseDTO updateTodo(Long id, TodoRequestDTO requestDTO, User currentUser) {
        validateTitle(requestDTO);
        Todo todo = findTodoOrThrow(id);

        todoMapper.updateEntityFromDTO(requestDTO, todo);

        if (requestDTO.getAssigneeId() != null
                && !requestDTO.getAssigneeId().equals(todo.getAssignee().getId())) {
            User assignee = userRepository.findById(requestDTO.getAssigneeId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Không tìm thấy nhân viên với id: " + requestDTO.getAssigneeId()));
            todo.setAssignee(assignee);
        }

        Todo saved = todoRepository.save(todo);
        return todoMapper.toResponseDTO(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TodoResponseDTO updateStatus(Long id, TodoStatus status, User currentUser) {
        Todo todo = findTodoOrThrow(id);
        assertCanView(todo, currentUser);

        todo.setStatus(status);
        Todo saved = todoRepository.save(todo);

        if (status == TodoStatus.COMPLETED && currentUser.getRole() == Role.EMPLOYEE) {
            notificationService.notifyTaskCompleted(saved);
        }

        return todoMapper.toResponseDTO(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteTodo(Long id, User currentUser) {
        Todo todo = findTodoOrThrow(id);
        todoRepository.delete(todo);
    }

    private void validateTitle(TodoRequestDTO requestDTO) {
        if (requestDTO.getTitle() == null || requestDTO.getTitle().isBlank()) {
            throw new IllegalArgumentException("Tiêu đề không được để trống");
        }
    }

    private void assertCanView(Todo todo, User currentUser) {
        boolean isOwner = todo.getAssignee() != null && todo.getAssignee().getId().equals(currentUser.getId());
        if (currentUser.getRole() == Role.EMPLOYEE && !isOwner) {
            throw new AccessDeniedException("Bạn không có quyền truy cập công việc này");
        }
    }

    private Todo findTodoOrThrow(Long id) {
        return todoRepository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException("Không tìm thấy công việc với id: " + id));
    }

    private Sort.Direction resolveDirection(String sortDir) {
        return "ASC".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
    }

    private String resolveSortProperty(String sortBy) {
        return switch (sortBy) {
            case "title" -> "title";
            case "dueDate" -> "dueDate";
            case "priority" -> "priority";
            case "status" -> "status";
            case "updatedAt" -> "updatedAt";
            default -> "createdAt";
        };
    }
}
