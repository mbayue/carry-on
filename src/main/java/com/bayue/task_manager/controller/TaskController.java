package com.bayue.task_manager.controller;

import com.bayue.task_manager.dto.request.CreateTaskRequest;
import com.bayue.task_manager.dto.request.UpdateTaskRequest;
import com.bayue.task_manager.dto.response.ApiResponse;
import com.bayue.task_manager.dto.response.TaskResponse;
import com.bayue.task_manager.entity.TaskStatus;
import com.bayue.task_manager.security.CustomUserDetails;
import com.bayue.task_manager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
        @Valid @RequestBody CreateTaskRequest request,
        @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        TaskResponse response = taskService.createTask(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(HttpStatus.CREATED.value(), "Task created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasks(
        @RequestParam(required = false) TaskStatus status,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
        @RequestParam(required = false) UUID user
    ) {
        List<TaskResponse> response = taskService.getTasks(status, dueDate, user);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Tasks retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTask(@PathVariable UUID id) {
        TaskResponse response = taskService.getTaskById(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Task retrieved successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
        @PathVariable UUID id,
        @RequestBody UpdateTaskRequest request
    ) {
        TaskResponse response = taskService.updateTask(id, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Task updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TaskResponse>> deleteTask(@PathVariable UUID id) {
        TaskResponse response = taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Task deleted successfully", response));
    }
}
