package com.bayue.task_manager.dto.response;

import com.bayue.task_manager.entity.Task;
import com.bayue.task_manager.entity.TaskStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaskResponse(
    UUID id,
    String title,
    String description,
    TaskStatus status,
    LocalDateTime dueDate,

    @JsonProperty("assignedTo")
    UUID assignedToId,

    String assignedToName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static TaskResponse fromEntity(Task task) {
        return new TaskResponse(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            task.getDueDate(),
            task.getAssignedTo() != null ? task.getAssignedTo().getId() : null,
            task.getAssignedTo() != null ? task.getAssignedTo().getName() : null,
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
    }
}
