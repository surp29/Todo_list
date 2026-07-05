package com.example.todolist.controller;

import com.example.todolist.dto.PageResponseDTO;
import com.example.todolist.dto.TodoRequestDTO;
import com.example.todolist.dto.TodoResponseDTO;
import com.example.todolist.dto.TodoStatusUpdateDTO;
import com.example.todolist.exception.TodoNotFoundException;
import com.example.todolist.model.Role;
import com.example.todolist.model.TodoPriority;
import com.example.todolist.model.TodoStatus;
import com.example.todolist.model.User;
import com.example.todolist.security.AppUserDetailsService;
import com.example.todolist.security.JwtService;
import com.example.todolist.service.TodoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TodoController.class)
@AutoConfigureMockMvc(addFilters = false)
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TodoService todoService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AppUserDetailsService appUserDetailsService;

    private final User leader = User.builder().id(1L).username("leader").fullName("Truong nhom").role(Role.LEADER).build();

    private MockHttpServletRequestBuilder asLeader(MockHttpServletRequestBuilder builder) {
        var auth = new UsernamePasswordAuthenticationToken(leader, null, leader.getAuthorities());
        return builder.with(SecurityMockMvcRequestPostProcessors.authentication(auth));
    }

    private TodoResponseDTO sampleResponse() {
        return TodoResponseDTO.builder()
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
    void test_GET_todos_returns200() throws Exception {
        PageResponseDTO<TodoResponseDTO> page = PageResponseDTO.<TodoResponseDTO>builder()
                .content(List.of(sampleResponse()))
                .currentPage(0)
                .totalPages(1)
                .totalElements(1)
                .pageSize(10)
                .isFirst(true)
                .isLast(true)
                .build();

        when(todoService.getAllTodos(any(), any(), any(), any(), anyInt(), anyInt(), anyString(), anyString(), any()))
                .thenReturn(page);

        mockMvc.perform(asLeader(get("/api/v1/todos")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    @Test
    void test_GET_todo_byId_returns200() throws Exception {
        when(todoService.getTodoById(any(), any())).thenReturn(sampleResponse());

        mockMvc.perform(asLeader(get("/api/v1/todos/1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Học Spring Boot"));
    }

    @Test
    void test_GET_todo_byId_notFound_returns404() throws Exception {
        when(todoService.getTodoById(any(), any()))
                .thenThrow(new TodoNotFoundException("Không tìm thấy công việc với id: 99"));

        mockMvc.perform(asLeader(get("/api/v1/todos/99")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void test_POST_todo_validRequest_returns201() throws Exception {
        TodoRequestDTO request = TodoRequestDTO.builder()
                .title("Học Spring Boot")
                .status(TodoStatus.PENDING)
                .priority(TodoPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(3))
                .assigneeId(2L)
                .build();

        when(todoService.createTodo(any(), any())).thenReturn(sampleResponse());

        mockMvc.perform(asLeader(post("/api/v1/todos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void test_POST_todo_invalidRequest_returns400() throws Exception {
        TodoRequestDTO request = TodoRequestDTO.builder()
                .title("")
                .status(null)
                .priority(null)
                .assigneeId(null)
                .build();

        mockMvc.perform(asLeader(post("/api/v1/todos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void test_PUT_todo_returns200() throws Exception {
        TodoRequestDTO request = TodoRequestDTO.builder()
                .title("Học Spring Boot nâng cao")
                .status(TodoStatus.IN_PROGRESS)
                .priority(TodoPriority.MEDIUM)
                .assigneeId(2L)
                .build();

        when(todoService.updateTodo(anyLong(), any(), any())).thenReturn(sampleResponse());

        mockMvc.perform(asLeader(put("/api/v1/todos/1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void test_PATCH_todo_status_returns200() throws Exception {
        TodoStatusUpdateDTO statusUpdate = TodoStatusUpdateDTO.builder()
                .status(TodoStatus.COMPLETED)
                .build();

        when(todoService.updateStatus(anyLong(), any(), any())).thenReturn(sampleResponse());

        mockMvc.perform(asLeader(patch("/api/v1/todos/1/status"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void test_DELETE_todo_returns204() throws Exception {
        mockMvc.perform(asLeader(delete("/api/v1/todos/1")))
                .andExpect(status().isNoContent());
    }
}
