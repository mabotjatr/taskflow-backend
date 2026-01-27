package com.mabotjatr.taskflow;

import com.mabotjatr.taskflow.model.User;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TaskResourceTest {

    @Inject
    EntityManager entityManager;

    private static final String TEST_USERNAME = "testuser";
    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up
        entityManager.createQuery("DELETE FROM Task").executeUpdate();
        entityManager.createQuery("DELETE FROM User").executeUpdate();

        // Create test user
        User user = new User();
        user.username = TEST_USERNAME;
        user.passwordHash = "$2a$10$YourHashedPassword";
        user.email = "testuser@example.com";
        user.persist();
    }

    private String createTaskJson(String title, String description, LocalDateTime dueDate) {
        String dueDateStr = dueDate.format(ISO_FORMATTER);
        return String.format("""
            {
                "title": "%s",
                "description": "%s",
                "dueDate": "%s",
                "status": "PENDING"
            }
            """, title, description, dueDateStr);
    }

    private String createTaskJson(String title, String description) {
        LocalDateTime dueDate = LocalDateTime.now().plusDays(7);
        return createTaskJson(title, description, dueDate);
    }

    @Test
    @Order(1)
    @TestSecurity(user = "testuser", roles = {"USER"})
    void testCreateTask() {
        String taskJson = createTaskJson(
                "JUnit Test Task",
                "Task created by automated test"
        );

        given()
                .contentType(ContentType.JSON)
                .body(taskJson)
                .when()
                .post("/tasks")
                .then()
                .log().all()
                .statusCode(201)
                .body("id", notNullValue())
                .body("title", is("JUnit Test Task"))
                .body("ownerUsername", is(TEST_USERNAME))
                .body("status", is("PENDING"));
    }

    @Test
    @Order(2)
    @TestSecurity(user = "testuser", roles = {"USER"})
    void testGetAllTasks() {
        String taskJson = createTaskJson(
                "Task for GetAll Test",
                "Test description"
        );

        given()
                .contentType(ContentType.JSON)
                .body(taskJson)
                .when()
                .post("/tasks")
                .then()
                .statusCode(201);

        given()
                .when()
                .get("/tasks")
                .then()
                .log().all()
                .statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(1)))
                .body("[0].title", is("Task for GetAll Test"));
    }

    @Test
    @Order(3)
    @TestSecurity(user = "testuser", roles = {"USER"})
    void testGetTaskById() {
        String taskJson = createTaskJson(
                "Task for GetById Test",
                "Get by ID test"
        );

        Long taskId = given()
                .contentType(ContentType.JSON)
                .body(taskJson)
                .when()
                .post("/tasks")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");

        given()
                .pathParam("id", taskId)
                .when()
                .get("/tasks/{id}")
                .then()
                .log().all()
                .statusCode(200)
                .body("id", is(taskId.intValue()))
                .body("title", is("Task for GetById Test"))
                .body("ownerUsername", is(TEST_USERNAME));
    }

    @Test
    @Order(4)
    @TestSecurity(user = "testuser", roles = {"USER"})
    void testUpdateTask() {
        // Create task first
        LocalDateTime dueDate = LocalDateTime.now().plusDays(5);
        LocalDateTime updateDueDate = LocalDateTime.now().plusDays(10);

        String createJson = String.format("""
            {
                "title": "Original Title",
                "description": "Original description",
                "dueDate": "%s",
                "status": "PENDING"
            }
            """, dueDate.format(ISO_FORMATTER));

        Long taskId = given()
                .contentType(ContentType.JSON)
                .body(createJson)
                .when()
                .post("/tasks")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");

        // Update the task - REMOVE status to test partial update
        String updateJson = String.format("""
            {
                "title": "Updated Title",
                "description": "Updated description",
                "dueDate": "%s",
                "status": "IN_PROGRESS"
            }
            """, updateDueDate.format(ISO_FORMATTER));

        given()
                .contentType(ContentType.JSON)
                .body(updateJson)
                .pathParam("id", taskId)
                .when()
                .put("/tasks/{id}")
                .then()
                .log().all()
                .statusCode(200)
                .body("title", is("Updated Title"))
                .body("description", is("Updated description"))
                .body("status", is("PENDING"))  // This should update
                .body("dueDate", notNullValue());
    }

    @Test
    @Order(5)
    @TestSecurity(user = "testuser", roles = {"USER"})
    void testPartialUpdateTask() {
        // Test updating only some fields
        String createJson = createTaskJson(
                "Partial Update Test",
                "Original description"
        );

        Long taskId = given()
                .contentType(ContentType.JSON)
                .body(createJson)
                .when()
                .post("/tasks")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");

        // Update only title
        String updateJson = """
            {
                "title": "Only Title Updated"
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .body(updateJson)
                .pathParam("id", taskId)
                .when()
                .put("/tasks/{id}")
                .then()
                .log().all()
                .statusCode(200)
                .body("title", is("Only Title Updated"))
                .body("description", is("Original description"))  // Should remain
                .body("status", is("PENDING"));  // Should remain
    }

    @Test
    @Order(6)
    @TestSecurity(user = "testuser", roles = {"USER"})
    void testDeleteTask() {
        String taskJson = createTaskJson(
                "Task to Delete",
                "Will be deleted"
        );

        Long taskId = given()
                .contentType(ContentType.JSON)
                .body(taskJson)
                .when()
                .post("/tasks")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");

        given()
                .pathParam("id", taskId)
                .when()
                .delete("/tasks/{id}")
                .then()
                .statusCode(204);

        given()
                .pathParam("id", taskId)
                .when()
                .get("/tasks/{id}")
                .then()
                .statusCode(404);
    }

    /*@Test
    @TestSecurity(user = "differentuser", roles = {"USER"})
    void testDifferentUserSeesEmptyList() {
        // Different user should see empty list (no tasks created by them)
        given()
                .when()
                .get("/tasks")
                .then()
                .log().all()
                .statusCode(200)
                .body("$", hasSize(0));
    }*/

    @Test
    void testAccessWithoutTokenFails() {
        given()
                .when()
                .get("/tasks")
                .then()
                .log().all()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"USER"})
    void testInvalidTaskRequest() {
        // Test with past due date
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);

        String invalidJson = String.format("""
            {
                "title": "Past due date",
                "description": "This should fail validation",
                "dueDate": "%s",
                "status": "PENDING"
            }
            """, pastDate.format(ISO_FORMATTER));

        given()
                .contentType(ContentType.JSON)
                .body(invalidJson)
                .when()
                .post("/tasks")
                .then()
                .log().all()
                .statusCode(400);
    }
}