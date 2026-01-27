package com.mabotjatr.taskflow.resource;

import com.mabotjatr.taskflow.dto.TaskRequest;
import com.mabotjatr.taskflow.dto.TaskResponse;
import com.mabotjatr.taskflow.model.Task;
import com.mabotjatr.taskflow.model.User;
import com.mabotjatr.taskflow.service.TaskService;
import com.mabotjatr.taskflow.service.UserService;
import com.mabotjatr.taskflow.util.TaskMapper;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;


@Path("/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("USER") // This secures ALL endpoints in this resource
@Tag(name = "Tasks", description = "Task management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class TaskResource {

    private final TaskService taskService;
    private final UserService userService;
    private final TaskMapper taskMapper;

    // Constructor injection
    @Inject
    public TaskResource(TaskService taskService, UserService userService, TaskMapper taskMapper) {
        this.taskService = taskService;
        this.userService = userService;
        this.taskMapper = taskMapper;
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
    public Response getAllTasks(@QueryParam("page") @DefaultValue("0") int page,
                                @QueryParam("size") @DefaultValue("20") int size) {

        var currentUser = userService.getCurrentUser();
        List<TaskResponse> tasks = taskService.getUserTasks(currentUser, page, size);
        return Response.ok(tasks).build();
    }


    /**
     * GET /tasks/{id} - Get a specific task by ID
     *
     * @param id a task id
     * @return a task
     */
    @GET
    @Path("/{id}")
    @Operation(
            summary = "Get task by ID",
            description = "Retrieve a specific task by its ID")
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Task found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Task.class))),
            @APIResponse(responseCode = "404", description = "Task not found")})
    public TaskResponse getTaskById(@Parameter(description = "Task ID", required = true, example = "1") @PathParam("id") Long id) {

        User currentUser = userService.getCurrentUser();
        return taskService.getTaskById(id, currentUser);
    }

    /**
     * POST /tasks - Create a new task
     *
     * @param taskRequest a task object
     * @return a new created task
     */
    @POST
    @Transactional
    @Operation(
            summary = "Create a new task",
            description = "Create a new task for the authenticated user")
    @APIResponses({
            @APIResponse(
                    responseCode = "201",
                    description = "Task created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Task.class))),
            @APIResponse(responseCode = "400", description = "Invalid task data")})
    public Response createTask(@Valid TaskRequest taskRequest) {

        var currentUser = userService.getCurrentUser();
        TaskResponse createdTask = taskService.createTask(currentUser, taskRequest);
        return Response.status(Response.Status.CREATED)
                .entity(createdTask)
                .build();
    }

    /**
     * PUT /tasks/{id} - Update an existing task
     *
     * @param id          task id
     * @param taskRequest task request object to be updated
     * @return updated task object
     */
    @PUT
    @Path("/{id}")
    @Transactional
    public TaskResponse updateTask(@PathParam("id") Long id, TaskRequest taskRequest)
    {
        var currentUser = userService.getCurrentUser();
        return taskService.updateTask(id, currentUser, taskRequest);
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
    public Response deleteTask(@PathParam("id") Long id)
    {
        var currentUser = userService.getCurrentUser();
        taskService.deleteTask(id, currentUser);
        return Response.noContent().build();
    }
}