package com.mabotjatr.taskflow.util;

import com.mabotjatr.taskflow.dto.TaskRequest;
import com.mabotjatr.taskflow.dto.TaskResponse;
import com.mabotjatr.taskflow.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "cdi")
public interface TaskMapper {

    @Mapping(source = "owner.id", target = "ownerId")
    @Mapping(source = "owner.username", target = "ownerUsername")
    TaskResponse toResponse(Task task);

    // TaskRequest -> Task (for creation)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "dueDate", ignore = true)
    Task toEntity(TaskRequest taskRequest);

    // Update Task from TaskRequest
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "dueDate", ignore = true)
    void updateEntity(TaskRequest taskRequest, @MappingTarget Task task);

    List<TaskResponse> toResponseList(List<Task> tasks);
}