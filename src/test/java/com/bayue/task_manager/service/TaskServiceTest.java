package com.bayue.task_manager.service;

import com.bayue.task_manager.dto.request.CreateTaskRequest;
import com.bayue.task_manager.dto.request.UpdateTaskRequest;
import com.bayue.task_manager.dto.response.TaskResponse;
import com.bayue.task_manager.entity.Role;
import com.bayue.task_manager.entity.Task;
import com.bayue.task_manager.entity.TaskStatus;
import com.bayue.task_manager.entity.User;
import com.bayue.task_manager.exception.BadRequestException;
import com.bayue.task_manager.exception.ResourceNotFoundException;
import com.bayue.task_manager.repository.TaskRepository;
import com.bayue.task_manager.repository.UserRepository;
import com.bayue.task_manager.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private User sampleUser;
    private Task sampleTask;
    private UUID taskId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        taskId = UUID.randomUUID();

        sampleUser = User.builder()
            .id(userId)
            .name("John Doe")
            .email("john@example.com")
            .password("password")
            .role(Role.USER)
            .build();

        sampleTask = Task.builder()
            .id(taskId)
            .title("Test Task")
            .description("Description")
            .status(TaskStatus.PENDING)
            .dueDate(LocalDateTime.now().plusDays(2))
            .assignedTo(sampleUser)
            .build();
    }

    @Test
    @DisplayName("createTask: Success with explicit assignedTo ID")
    void createTask_WithExplicitAssignee_Success() {
        CreateTaskRequest request = new CreateTaskRequest(
            "New Task",
            "Task Description",
            TaskStatus.IN_PROGRESS,
            LocalDateTime.now().plusDays(3),
            userId
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        TaskResponse response = taskService.createTask(request, null);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(taskId);
        verify(userRepository, times(1)).findById(userId);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("createTask: Defaults to currently authenticated user if assignedTo is null")
    void createTask_DefaultsToCurrentUser_Success() {
        CustomUserDetails currentUser = new CustomUserDetails(sampleUser);
        CreateTaskRequest request = new CreateTaskRequest(
            "New Task",
            "Task Description",
            null,
            LocalDateTime.now().plusDays(3),
            null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        TaskResponse response = taskService.createTask(request, currentUser);

        assertThat(response).isNotNull();
        verify(userRepository, times(1)).findById(userId);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("createTask: Throws BadRequestException when assigned user is not found")
    void createTask_UserNotFound_ThrowsBadRequest() {
        UUID unknownUserId = UUID.randomUUID();
        CreateTaskRequest request = new CreateTaskRequest(
            "New Task",
            "Task Description",
            TaskStatus.PENDING,
            LocalDateTime.now().plusDays(3),
            unknownUserId
        );

        when(userRepository.findById(unknownUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(request, null))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("User not found");

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("getTasks: Returns filtered list of TaskResponse")
    void getTasks_Success() {
        when(taskRepository.findAll(any(Specification.class))).thenReturn(List.of(sampleTask));

        List<TaskResponse> responses = taskService.getTasks(TaskStatus.PENDING, LocalDate.now(), userId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).title()).isEqualTo("Test Task");
    }

    @Test
    @DisplayName("getTaskById: Returns TaskResponse when found")
    void getTaskById_Found_Success() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(sampleTask));

        TaskResponse response = taskService.getTaskById(taskId);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(taskId);
        assertThat(response.assignedToName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("getTaskById: Throws ResourceNotFoundException when not found")
    void getTaskById_NotFound_ThrowsException() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(taskId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Task not found with id: " + taskId);
    }

    @Test
    @DisplayName("updateTask: Successfully updates fields and assignee")
    void updateTask_Success() {
        UUID newUserId = UUID.randomUUID();
        User newAssignee = User.builder().id(newUserId).name("Alice").email("alice@example.com").build();

        UpdateTaskRequest request = new UpdateTaskRequest(
            "Updated Title",
            "Updated Description",
            TaskStatus.DONE,
            LocalDateTime.now().plusDays(5),
            newUserId
        );

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(sampleTask));
        when(userRepository.findById(newUserId)).thenReturn(Optional.of(newAssignee));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.updateTask(taskId, request);

        assertThat(response.title()).isEqualTo("Updated Title");
        assertThat(response.description()).isEqualTo("Updated Description");
        assertThat(response.status()).isEqualTo(TaskStatus.DONE);
        assertThat(response.assignedToId()).isEqualTo(newUserId);
    }

    @Test
    @DisplayName("updateTask: Throws ResourceNotFoundException when task does not exist")
    void updateTask_NotFound_ThrowsException() {
        UpdateTaskRequest request = new UpdateTaskRequest("Updated", null, null, null, null);

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateTask(taskId, request))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("deleteTask: Deletes and returns deleted task")
    void deleteTask_Success() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(sampleTask));

        TaskResponse response = taskService.deleteTask(taskId);

        assertThat(response.id()).isEqualTo(taskId);
        verify(taskRepository, times(1)).delete(sampleTask);
    }

    @Test
    @DisplayName("deleteTask: Throws ResourceNotFoundException when task does not exist")
    void deleteTask_NotFound_ThrowsException() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.deleteTask(taskId))
            .isInstanceOf(ResourceNotFoundException.class);

        verify(taskRepository, never()).delete(any(Task.class));
    }
}
