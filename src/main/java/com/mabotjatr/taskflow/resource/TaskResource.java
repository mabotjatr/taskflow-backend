package com.mabotjatr.taskflow.resource;

import com.mabotjatr.taskflow.dto.TaskResponse;
import com.mabotjatr.taskflow.model.Task;
import com.mabotjatr.taskflow.model.User;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.LocalDateTime;
import java.util.List;

@Path("/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("USER") // This secures ALL endpoints in this resource
public class TaskResource {

    @Inject
    SecurityIdentity jIdentity; // Injected JWT token from the request

    /**
     * Helper method to get the current authenticated user
     *
     * @return a user object
     */
    private User getCurrentUser() {
        String username = jIdentity.getPrincipal().getName();
        User user = User.findByUsername(username);
        if (user == null) {
            throw new WebApplicationException("User not found", Response.Status.UNAUTHORIZED);
        }

        System.out.println("**************************** user : " + user.username);
        return user;
    }

    /**
     * Maps the task object to the task response DTO
     *
     * @param task a task object
     * @return task response DTO
     */
    private TaskResponse toResponse(Task task) {
        TaskResponse dto = new TaskResponse();
        dto.id = task.id;
        dto.title = task.title;
        dto.description = task.description;
        dto.status = task.status;
        dto.dueDate = task.dueDate;
        dto.createdAt = task.createdAt;
        dto.ownerUsername = task.owner.username;
        return dto;
    }

    /**
     * GET /tasks - Get all tasks for the current user
     *
     * @return a list of tasks
     */
    @GET
    public List<TaskResponse> getAllTasks() {
        System.out.println("************************ List all the tasks *****************************");
        return Task.findByUser(getCurrentUser())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * GET /tasks/{id} - Get a specific task by ID
     *
     * @param id  a task id
     * @return a task
     */
    @GET
    @Path("/{id}")
    public Task getTaskById(@PathParam("id") Long id) {
        User currentUser = getCurrentUser();
        Task task = Task.findById(id);

        // Security check: ensure the task belongs to the current user
        if (task == null || !task.owner.id.equals(currentUser.id)) {
            throw new WebApplicationException("Task not found", Response.Status.NOT_FOUND);
        }
        return task;
    }

    /**
     * POST /tasks - Create a new task
     *
     * @param newTask a task object
     * @return a new created task
     */
    @POST
    @Transactional
    public Response createTask(Task newTask) {

        newTask.owner = getCurrentUser();
        newTask.createdAt = LocalDateTime.now();
        if (newTask.status == null) {
            newTask.status = Task.Status.PENDING;
        }

        newTask.persist();

        System.out.println("****************** New Task created : "+ newTask.toString());

        return Response.status(Response.Status.CREATED)
                .entity(toResponse(newTask))
                .build();
    }

    /**
     * PUT /tasks/{id} - Update an existing task
     *
     * @param id  task id
     * @param updatedTask task object to be updated
     * @return updated task object
     */
    @PUT
    @Path("/{id}")
    @Transactional
    public TaskResponse updateTask(@PathParam("id") Long id, Task updatedTask) {
        System.out.println("*************************update task********************************");
        User currentUser = getCurrentUser();
        Task existingTask = (Task) Task.findByIdOptional(id).orElseThrow(() -> new WebApplicationException("Task not found", 404));

        if (!existingTask.owner.id.equals(currentUser.id)) {
            throw new WebApplicationException("Forbidden", 403);
        }

        // Update only allowed fields
        existingTask.title = updatedTask.title;
        existingTask.description = updatedTask.description;
        existingTask.status = updatedTask.status;
        existingTask.dueDate = updatedTask.dueDate;

        existingTask.persist();

        return toResponse(existingTask);
    }

    /**
     * DELETE /tasks/{id} - Delete a task
     *
     * @param id a task id to be deleted
     * @return updated task object
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteTask(@PathParam("id") Long id) {
        System.out.println("*************************deleting task********************************");

        User currentUser = getCurrentUser();
        Task task = (Task) Task.findByIdOptional(id).orElseThrow(() -> new WebApplicationException("Task not found", 404));

        if (!task.owner.id.equals(currentUser.id)) {
            throw new WebApplicationException("Forbidden", 403);
        }

        task.delete();

        return Response.noContent().build();
    }
}