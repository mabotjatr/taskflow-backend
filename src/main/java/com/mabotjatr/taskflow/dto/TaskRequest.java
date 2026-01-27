package com.mabotjatr.taskflow.dto;


import java.time.LocalDateTime;

import com.mabotjatr.taskflow.model.Task;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    public String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    public String description;

    @NotNull(message = "Due date is required")
    @FutureOrPresent(message = "Due date must be in the present or future")
    public LocalDateTime dueDate;

    public Task.Status status;
}