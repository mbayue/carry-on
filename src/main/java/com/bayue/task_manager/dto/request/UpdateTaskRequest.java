package com.bayue.task_manager.dto.request;

import com.bayue.task_manager.entity.TaskStatus;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateTaskRequest(
    String title,
    String description,
    TaskStatus status,
    LocalDateTime dueDate,

    @JsonProperty("assignedTo")
    @JsonAlias({"assignedTo", "assignedToId"})
    UUID assignedToId
) {}
