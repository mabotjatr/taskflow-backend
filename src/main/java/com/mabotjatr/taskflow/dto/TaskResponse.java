package com.mabotjatr.taskflow.dto;

import java.time.LocalDateTime;
import com.mabotjatr.taskflow.model.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse { // possibly make use of  records

    public Long id;
    public String title;
    public String description;
    public Task.Status status;
    public LocalDateTime dueDate;
    public LocalDateTime createdAt;
    public String ownerUsername;
    public Long ownerId;
}
