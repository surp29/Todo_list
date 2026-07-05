package com.example.todolist.service;

import com.example.todolist.dto.PageResponseDTO;
import com.example.todolist.dto.TodoRequestDTO;
import com.example.todolist.dto.TodoResponseDTO;
import com.example.todolist.model.TodoPriority;
import com.example.todolist.model.TodoStatus;
import com.example.todolist.model.User;

/**
 * Business logic contract for managing Todo items.
 * <p>
 * Every method takes the currently authenticated {@link User} so it can enforce
 * that a Leader has full access while an Employee is scoped to tasks assigned to them.
 */
public interface TodoService {

    /**
     * Creates a new todo item and assigns it to another user. Only a Leader may call this.
     *
     * @param requestDTO  the data used to create the todo, including the assignee's id
     * @param currentUser the authenticated Leader creating the task
     * @return the created todo as a response DTO
     */
    TodoResponseDTO createTodo(TodoRequestDTO requestDTO, User currentUser);

    /**
     * Retrieves a single todo by its identifier. An Employee may only fetch a todo
     * assigned to them; a Leader may fetch any todo.
     *
     * @param id          the todo identifier
     * @param currentUser the authenticated user making the request
     * @return the matching todo as a response DTO
     */
    TodoResponseDTO getTodoById(Long id, User currentUser);

    /**
     * Retrieves a paginated, optionally filtered and sorted list of todos.
     * An Employee's results are always scoped to todos assigned to them, regardless
     * of the {@code assigneeId} filter; a Leader may filter by any assignee or none.
     *
     * @param keyword     optional text searched in title or description
     * @param status      optional status filter
     * @param priority    optional priority filter
     * @param assigneeId  optional assignee filter (Leader only; ignored for Employees)
     * @param page        zero-based page index
     * @param size        page size
     * @param sortBy      field name to sort by
     * @param sortDir     sort direction, {@code ASC} or {@code DESC}
     * @param currentUser the authenticated user making the request
     * @return a page of todos wrapped in {@link PageResponseDTO}
     */
    PageResponseDTO<TodoResponseDTO> getAllTodos(String keyword,
                                                  TodoStatus status,
                                                  TodoPriority priority,
                                                  Long assigneeId,
                                                  int page,
                                                  int size,
                                                  String sortBy,
                                                  String sortDir,
                                                  User currentUser);

    /**
     * Updates every field of an existing todo, including reassignment. Only a Leader may call this.
     *
     * @param id          the todo identifier
     * @param requestDTO  the new data for the todo
     * @param currentUser the authenticated Leader making the request
     * @return the updated todo as a response DTO
     */
    TodoResponseDTO updateTodo(Long id, TodoRequestDTO requestDTO, User currentUser);

    /**
     * Updates only the status of an existing todo. A Leader may update any todo;
     * an Employee may only update a todo assigned to them. When an Employee marks
     * their task {@code COMPLETED}, the Leader who created it is notified.
     *
     * @param id          the todo identifier
     * @param status      the new status
     * @param currentUser the authenticated user making the request
     * @return the updated todo as a response DTO
     */
    TodoResponseDTO updateStatus(Long id, TodoStatus status, User currentUser);

    /**
     * Deletes an existing todo. Only a Leader may call this.
     *
     * @param id          the todo identifier
     * @param currentUser the authenticated Leader making the request
     */
    void deleteTodo(Long id, User currentUser);
}
