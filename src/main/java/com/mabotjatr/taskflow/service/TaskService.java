package com.mabotjatr.taskflow.service;

import com.mabotjatr.taskflow.dto.TaskRequest;
import com.mabotjatr.taskflow.dto.TaskResponse;
import com.mabotjatr.taskflow.model.Task;
import com.mabotjatr.taskflow.model.User;
import com.mabotjatr.taskflow.respository.TaskRepository;
import com.mabotjatr.taskflow.util.TaskMapper;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final Logger logger;

    @Inject
    public TaskService(TaskRepository taskRepository, TaskMapper taskMapper, Logger logger)
    {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.logger = logger;
    }

    /**
     * Get paginated tasks for a user
     */
    public List<TaskResponse> getUserTasks(User user, int page, int size) {
        logger.info("********************* getting Task List ************************");
        List<Task> tasks = taskRepository.findByUser(user, page, size);
        logger.infof("************************ User %s fetching page %d of tasks", user.username, page);
        return taskMapper.toResponseList(tasks);
    }

    /**
     * Get specific task for a user (with security check)
     */
    public TaskResponse getTaskById(Long taskId, User user) {
        Task task = taskRepository.findByIdAndOwner(taskId, user)
                .orElseThrow(() -> new WebApplicationException("Task not found", Response.Status.NOT_FOUND));

        logger.debugf("********************* User %s fetching task %d", user.username, taskId);

        return taskMapper.toResponse(task);
    }

    /**
     * Create a new task for a user
     */
    @Transactional
    public TaskResponse createTask(User user, TaskRequest taskRequest) {
        Task task = taskMapper.toEntity(taskRequest);
        task.owner = user;
        task.createdAt = LocalDateTime.now();
        task.description = taskRequest.description;
        task.dueDate = taskRequest.dueDate;
        task.title = taskRequest.title;
        task.status = taskRequest.status;

        // Set default status if not provided
        if (task.status == null) {
            task.status = Task.Status.PENDING;
        }

        logger.infof("******************* User %s creating new task", user.username);
        taskRepository.persist(task);
        logger.infof("******************* User %s created new task successfully", user.username);

        return taskMapper.toResponse(task);
    }

    /**
     * Update an existing task
     */
    @Transactional
    public TaskResponse updateTask(Long taskId, User user, TaskRequest taskRequest) {

        logger.info("*************************update task********************************");

        Task task = taskRepository.findByIdAndOwner(taskId, user)
                .orElseThrow(() -> new WebApplicationException("Task not found", Response.Status.NOT_FOUND));

        // Update only allowed fields
        if (taskRequest.title!= null) {
            task.title = taskRequest.title;
        }
        if (taskRequest.description != null) {
            task.description = taskRequest.description;
        }
        if (taskRequest.dueDate != null) {
            task.dueDate = taskRequest.dueDate;
        }

        logger.infof("********************* User %s updating task %d", user.username, taskId);

        taskRepository.persist(task);
        return taskMapper.toResponse(task);
    }

    /**
     * Delete a task
     */
    @Transactional
    public void deleteTask(Long taskId, User user) {
        Task task = taskRepository.findByIdAndOwner(taskId, user)
                .orElseThrow(() -> new WebApplicationException("Task not found", Response.Status.NOT_FOUND));

        taskRepository.delete(task);
    }

    /**
     * Update task status
     */
    @Transactional
    public TaskResponse updateTaskStatus(Long taskId, User user, Task.Status status) {

        logger.info("*************************deleting task********************************");

        Task task = taskRepository.findByIdAndOwner(taskId, user)
                .orElseThrow(() -> new WebApplicationException("Task not found", Response.Status.NOT_FOUND));

        task.status = status;
        taskRepository.persist(task);
        return taskMapper.toResponse(task);
    }

    /**
     * Get task count for user
     */
    public long getTaskCount(User user) {
        return taskRepository.countByUser(user);
    }

    /**
     * Search tasks by title
     */
    public List<TaskResponse> searchTasks(User user, String keyword, int page, int size) {
        List<Task> tasks = taskRepository
                .find("owner = ?1 and (lower(title) like lower(?2) or lower(description) like lower(?2))",
                        user, "%" + keyword + "%")
                .page(Page.of(page, size))
                .list();

        return taskMapper.toResponseList(tasks);
    }
}