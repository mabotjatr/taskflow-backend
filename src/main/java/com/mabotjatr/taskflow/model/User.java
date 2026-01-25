package com.mabotjatr.taskflow.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "task_user")
public class User extends PanacheEntity {

    @Column(unique = true, nullable = false)
    public String username;

    @Column(nullable = false)
    public String email;

    @Column(name="password", nullable = false)
    public String passwordHash; // Store a BCrypt hash, never plain text

    // One user can have many tasks
    @OneToMany(mappedBy = "owner")
    public List<Task> tasks;
    public String roles = "USER";

    public User() {
    }

    /**
     * Helper method to get roles as a list
     *
     * @return
     */
    public List<String> getRolesList() {
        return Arrays.asList(roles.split(","));
    }

    /**
     * Helper method to find a user by username
     *
     * @param username
     * @return
     */
    public static User findByUsername(String username) {
        return find("username", username).firstResult();
    }

}
