package com.mabotjatr.taskflow.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Task extends PanacheEntity {

    public String title;
    public String description;

    @Enumerated(EnumType.STRING)
    public Status status = Status.PENDING; // Use an Enum: PENDING, IN_PROGRESS, COMPLETED

    public LocalDateTime dueDate;
    public LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    public User owner; // Each task belongs to a user

    public enum Status {
        PENDING, IN_PROGRESS, COMPLETED
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

