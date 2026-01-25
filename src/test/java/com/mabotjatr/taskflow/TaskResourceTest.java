package com.mabotjatr.taskflow;

import com.mabotjatr.taskflow.model.User;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;

import io.quarkus.test.security.TestSecurity;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TaskResourceTest {

    private static Long createdTaskId;

    @Inject
    EntityManager entityManager;

    @BeforeEach
    @Transactional
    void cleanDatabase() {
        entityManager.createQuery("DELETE FROM Task").executeUpdate();
        entityManager.createQuery("DELETE FROM User").executeUpdate();

        User user = new User();
        user.username = "testuser";
        user.passwordHash = "password123";
        user.email = "postman@test.com";
        user.persist();
    }

    @Test
    @Order(1)
    @TestSecurity(user = "testuser", roles = {"USER"})
    void testCreateTask() {

        String taskJson = """
            {
                "title": "JUnit Test Task",
                "description": "Task created by automated test",
                "status": "PENDING"
            }
            """;

        createdTaskId = given()
                .contentType(ContentType.JSON)
                .body(taskJson)
                .when()
                .post("/tasks")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");
    }

    @Test
    @Order(2)
    @TestSecurity(user = "testuser", roles = {"USER"})
    void testGetAllTasks() {
        given()
                .when().get("/tasks")
                .then()
                .statusCode(200);
    }

    @Test
    void testAccessWithoutTokenFails() {
        given()
                .when().get("/tasks")
                .then()
                .statusCode(401);
    }
}
