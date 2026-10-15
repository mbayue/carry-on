package com.bayue.task_manager.service;

import com.bayue.task_manager.dto.request.CreateTaskRequest;
import com.bayue.task_manager.dto.request.UpdateTaskRequest;
import com.bayue.task_manager.dto.response.TaskResponse;
import com.bayue.task_manager.entity.Task;
import com.bayue.task_manager.entity.TaskStatus;
import com.bayue.task_manager.entity.User;
import com.bayue.task_manager.exception.BadRequestException;
import com.bayue.task_manager.exception.ResourceNotFoundException;
import com.bayue.task_manager.repository.TaskRepository;
import com.bayue.task_manager.repository.TaskSpecification;
import com.bayue.task_manager.repository.UserRepository;
import com.bayue.task_manager.security.CustomUserDetails;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    @CacheEvict(value = "tasks", allEntries = true)
    public TaskResponse createTask(CreateTaskRequest request, CustomUserDetails currentUser) {
        UUID targetUserId = request.assignedToId() != null
            ? request.assignedToId()
            : (currentUser != null ? currentUser.getId() : null);

        User assignedUser = null;
        if (targetUserId != null) {
            assignedUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        }

        Task task = Task.builder()
            .title(request.title())
            .description(request.description())
            .status(request.status() != null ? request.status() : TaskStatus.PENDING)
            .dueDate(request.dueDate())
            .assignedTo(assignedUser)
            .build();

        return TaskResponse.fromEntity(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasks(TaskStatus status, LocalDate dueDate, UUID userId) {
        return taskRepository.findAll(TaskSpecification.filter(status, dueDate, userId))
            .stream()
            .map(TaskResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "tasks", key = "#id")
    public TaskResponse getTaskById(UUID id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return TaskResponse.fromEntity(task);
    }

    @Transactional
    @CacheEvict(value = "tasks", allEntries = true)
    public TaskResponse updateTask(UUID id, UpdateTaskRequest request) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        if (request.title() != null) {
            task.setTitle(request.title());
        }
        if (request.description() != null) {
            task.setDescription(request.description());
        }
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        if (request.dueDate() != null) {
            task.setDueDate(request.dueDate());
        }
        if (request.assignedToId() != null) {
            User assignedUser = userRepository.findById(request.assignedToId())
                .orElseThrow(() -> new BadRequestException("User not found"));
            task.setAssignedTo(assignedUser);
        }

        return TaskResponse.fromEntity(taskRepository.save(task));
    }

    @Transactional
    @CacheEvict(value = "tasks", allEntries = true)
    public TaskResponse deleteTask(UUID id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        taskRepository.delete(task);
        return TaskResponse.fromEntity(task);
    }
}
