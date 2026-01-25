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
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.List;

import static org.eclipse.microprofile.openapi.annotations.media.Schema.*;

@Path("/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("USER") // This secures ALL endpoints in this resource
@Tag(name = "Tasks", description = "Task management endpoints")
@SecurityRequirement(name = "bearerAuth")
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
    @Operation(
            summary = "Get all tasks",
            description = "Retrieve all tasks for the authenticated user"
    )
    @APIResponse(
            responseCode = "200",
            description = "List of tasks",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Task.class))
    )
    @APIResponse(
            responseCode = "401",
            description = "Unauthorized - invalid or missing token"
    )
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
    @Operation(
            summary = "Get task by ID",
            description = "Retrieve a specific task by its ID" )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Task found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Task.class)) ),
            @APIResponse( responseCode = "404",description = "Task not found" )})
    public Task getTaskById( @Parameter(description = "Task ID", required = true,example = "1" ) @PathParam("id")  Long id) {

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
    @Operation(
            summary = "Create a new task",
            description = "Create a new task for the authenticated user" )
    @APIResponses({
            @APIResponse(
                    responseCode = "201",
                    description = "Task created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Task.class))),
            @APIResponse( responseCode = "400", description = "Invalid task data" )})
    public Response createTask( @RequestBody( description = "Task data",required = true,
                    content = @Content( mediaType = "application/json",schema = @Schema(implementation = Task.class)) )Task newTask) {

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