package com.mabotjatr.taskflow.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Schema(description = "Task entity representing a user's task")
public class Task extends PanacheEntity {

    @Schema(description = "Task title", example = "Complete project documentation", required = true)
    public String title;

    @Schema(description = "Task description", example = "Write comprehensive docs for the TaskFlow API")
    public String description;

    @Enumerated(EnumType.STRING)
    @Schema(description = "Task status", implementation = Status.class, example = "PENDING")
    public Status status = Status.PENDING; // Use an Enum: PENDING, IN_PROGRESS, COMPLETED

    @Schema(description = "Due date for the task", example = "2024-12-31T23:59:59")
    public LocalDateTime dueDate;

    @Schema(description = "Task creation timestamp", example = "2024-01-25T10:30:00", readOnly = true)
    public LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    public User owner; // Each task belongs to a user

    @Schema(description = "Task status enum")
    public enum Status {
        @Schema(description = "Task is pending")
        PENDING,
        @Schema(description = "Task is in progress")
        IN_PROGRESS,
        @Schema(description = "Task is completed")
        COMPLETED
    }

    /**
     * Helper method to find all tasks for a specific user
     * @param user
     * @return
     */
    public static List<Task> findByUser(User user) {
        return list("owner", user);
    }

    @Override
    public String toString() {
        return "Task{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", dueDate=" + dueDate +
                ", createdAt=" + createdAt +
                ", owner=" + owner +
                '}';
    }
}

