package com.bayue.task_manager.dto.request;

import com.bayue.task_manager.entity.TaskStatus;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateTaskRequest(
    @NotBlank(message = "Title is required")
    String title,

    String description,

    TaskStatus status,

    @NotNull(message = "Due date is required")
    @Future(message = "Due date must be in the future")
    LocalDateTime dueDate,

    @JsonProperty("assignedTo")
    @JsonAlias({"assignedTo", "assignedToId"})
    UUID assignedToId
) {}
