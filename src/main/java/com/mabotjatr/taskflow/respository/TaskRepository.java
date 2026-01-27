package com.mabotjatr.taskflow.respository;

import com.mabotjatr.taskflow.model.Task;
import com.mabotjatr.taskflow.model.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class TaskRepository implements PanacheRepository<Task> {

    /**
     * Find tasks by user with pagination
     */
    public List<Task> findByUser(User user, int page, int size) {
        return find("owner", Sort.by("dueDate").and("createdAt", Sort.Direction.Descending), user)
                .page(Page.of(page, size))
                .list();
    }

    /**
     * Find task by ID and owner (for security)
     */
    public Optional<Task> findByIdAndOwner(Long taskId, User owner) {
        return find("id = ?1 and owner = ?2", taskId, owner).firstResultOptional();
    }

    /**
     * Find task by ID and owner ID
     */
    public Optional<Task> findByIdAndOwnerId(Long taskId, Long ownerId) {
        return find("id = ?1 and owner.id = ?2", taskId, ownerId).firstResultOptional();
    }

    /**
     * Count tasks by user
     */
    public long countByUser(User user) {
        return count("owner", user);
    }

    /**
     * Find overdue tasks for a user
     */
    public List<Task> findOverdueTasks(User user) {
        return find("owner = ?1 and dueDate < current_timestamp and status != 'COMPLETED'", user)
                .list();
    }

    /**
     * Find tasks by status for a user
     */
    public List<Task> findByUserAndStatus(User user, Task.Status status, int page, int size) {
        return find("owner = ?1 and status = ?2",
                Sort.by("dueDate").ascending(), user, status)
                .page(Page.of(page, size))
                .list();
    }
}