package com.bayue.task_manager.controller;

import com.bayue.task_manager.dto.request.CreateTaskRequest;
import com.bayue.task_manager.dto.request.UpdateTaskRequest;
import com.bayue.task_manager.dto.response.TaskResponse;
import com.bayue.task_manager.entity.TaskStatus;
import com.bayue.task_manager.exception.GlobalExceptionHandler;
import com.bayue.task_manager.exception.ResourceNotFoundException;
import com.bayue.task_manager.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private TaskResponse sampleTaskResponse;
    private UUID taskId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(taskController)
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

        taskId = UUID.randomUUID();
        sampleTaskResponse = new TaskResponse(
            taskId,
            "Sample Task",
            "Sample Description",
            TaskStatus.PENDING,
            LocalDateTime.now().plusDays(2),
            UUID.randomUUID(),
            "John Doe",
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("POST /api/tasks: Returns 201 Created on valid task request")
    void createTask_Success() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest(
            "Sample Task",
            "Sample Description",
            TaskStatus.PENDING,
            LocalDateTime.now().plusDays(3),
            null
        );

        when(taskService.createTask(any(CreateTaskRequest.class), any())).thenReturn(sampleTaskResponse);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.code").value(201))
            .andExpect(jsonPath("$.message").value("Task created successfully"))
            .andExpect(jsonPath("$.data.title").value("Sample Task"));
    }

    @Test
    @DisplayName("POST /api/tasks: Returns 400 Bad Request on empty title or missing dueDate")
    void createTask_ValidationError() throws Exception {
        CreateTaskRequest invalidRequest = new CreateTaskRequest(
            "",
            "Description",
            TaskStatus.PENDING,
            null,
            null
        );

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("bad_request"))
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("GET /api/tasks: Returns 200 OK with list of tasks")
    void getTasks_Success() throws Exception {
        when(taskService.getTasks(any(), any(), any())).thenReturn(List.of(sampleTaskResponse));

        mockMvc.perform(get("/api/tasks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data[0].title").value("Sample Task"));
    }

    @Test
    @DisplayName("GET /api/tasks/{id}: Returns 200 OK when task exists")
    void getTask_Success() throws Exception {
        when(taskService.getTaskById(taskId)).thenReturn(sampleTaskResponse);

        mockMvc.perform(get("/api/tasks/" + taskId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.id").value(taskId.toString()));
    }

    @Test
    @DisplayName("GET /api/tasks/{id}: Returns 404 Not Found when task does not exist")
    void getTask_NotFound() throws Exception {
        when(taskService.getTaskById(taskId))
            .thenThrow(new ResourceNotFoundException("Task not found with id: " + taskId));

        mockMvc.perform(get("/api/tasks/" + taskId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value("not_found"))
            .andExpect(jsonPath("$.code").value(404))
            .andExpect(jsonPath("$.message").value("Task not found with id: " + taskId));
    }

    @Test
    @DisplayName("PUT /api/tasks/{id}: Returns 200 OK when task is updated")
    void updateTask_Success() throws Exception {
        UpdateTaskRequest request = new UpdateTaskRequest(
            "Updated Task Title",
            "Updated Description",
            TaskStatus.DONE,
            LocalDateTime.now().plusDays(4),
            null
        );

        when(taskService.updateTask(eq(taskId), any(UpdateTaskRequest.class))).thenReturn(sampleTaskResponse);

        mockMvc.perform(put("/api/tasks/" + taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value("Task updated successfully"));
    }

    @Test
    @DisplayName("DELETE /api/tasks/{id}: Returns 200 OK with deleted task")
    void deleteTask_Success() throws Exception {
        when(taskService.deleteTask(taskId)).thenReturn(sampleTaskResponse);

        mockMvc.perform(delete("/api/tasks/" + taskId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value("Task deleted successfully"));
    }
}
