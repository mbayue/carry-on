package com.bayue.task_manager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tasks")
@Getter
@Setter
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.PENDING;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Task() {
    }

    public Task(
        UUID id,
        String title,
        String description,
        TaskStatus status,
        LocalDateTime dueDate,
        User assignedTo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status != null ? status : TaskStatus.PENDING;
        this.dueDate = dueDate;
        this.assignedTo = assignedTo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static TaskBuilder builder() {
        return new TaskBuilder();
    }

    public static class TaskBuilder {
        private UUID id;
        private String title;
        private String description;
        private TaskStatus status = TaskStatus.PENDING;
        private LocalDateTime dueDate;
        private User assignedTo;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public TaskBuilder id(UUID id) { this.id = id; return this; }
        public TaskBuilder title(String title) { this.title = title; return this; }
        public TaskBuilder description(String description) { this.description = description; return this; }
        public TaskBuilder status(TaskStatus status) { this.status = status; return this; }
        public TaskBuilder dueDate(LocalDateTime dueDate) { this.dueDate = dueDate; return this; }
        public TaskBuilder assignedTo(User assignedTo) { this.assignedTo = assignedTo; return this; }
        public TaskBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public TaskBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Task build() {
            return new Task(id, title, description, status, dueDate, assignedTo, createdAt, updatedAt);
        }
    }
}
