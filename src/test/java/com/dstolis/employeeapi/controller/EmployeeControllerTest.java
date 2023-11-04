package com.dstolis.employeeapi.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.kafka.test.context.EmbeddedKafka;

import com.dstolis.employeeapi.model.dto.EmployeeDTO;
import com.dstolis.employeeapi.repository.EmployeeRepository;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
class EmployeeControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        employeeRepository.deleteAll();
    }

    @Test
    void testCreateEmployee() {
        var employeeDTO = new EmployeeDTO(
            null, "testemail@sample.com", "Sample Name",
            LocalDate.of(1990, 1, 1), List.of("hiking", "reading")
        );

        given().auth().basic("admin", "admin")
            .contentType(ContentType.JSON)
            .body(employeeDTO)
            .when()
            .post("/api/employees")
            .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("email", is(employeeDTO.email()))
            .body("fullName", is(employeeDTO.fullName()))
            .body("birthday", is(employeeDTO.birthday().toString()))
            .body("hobbies", is(employeeDTO.hobbies()));
    }

    @Test
    void testCreateEmployee_ExistingEmail() {
        var initialEmployeeDTO = new EmployeeDTO(
            null, "existingemail@sample.com", "Existing Name",
            LocalDate.of(1990, 1, 1), List.of("hiking", "reading")
        );

        given().auth().basic("admin", "admin")
            .contentType(ContentType.JSON)
            .body(initialEmployeeDTO)
            .when()
            .post("/api/employees");

        var duplicateEmployeeDTO = new EmployeeDTO(
            null, "existingemail@sample.com", "Duplicate Name",
            LocalDate.of(1992, 2, 2), List.of("swimming", "writing")
        );

        given().auth().basic("admin", "admin")
            .contentType(ContentType.JSON)
            .body(duplicateEmployeeDTO)
            .when()
            .post("/api/employees")
            .then()
            .statusCode(400)
            .body("message", is("Email existingemail@sample.com already exists!"));
    }

    @Test
    void testUpdateEmployee() {
        var initialEmployeeDTO = new EmployeeDTO(
            null, "initial@sample.com", "Initial Name",
            LocalDate.of(1990, 1, 1), List.of("hiking", "swimming")
        );

        var id = given().auth().basic("admin", "admin")
            .contentType(ContentType.JSON)
            .body(initialEmployeeDTO)
            .when()
            .post("/api/employees")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath().getUUID("id");

        assertNotNull(id, "Created employee should have a non-null ID");

        // Update fields
        var updatedEmployeeDTO = new EmployeeDTO(
            null, "updated@sample.com", "Updated Name",
            LocalDate.of(2000, 1, 1), List.of("reading", "travelling")
        );

        given().auth().basic("admin", "admin")
            .contentType(ContentType.JSON)
            .body(updatedEmployeeDTO)
            .when()
            .put("/api/employees/" + id)
            .then()
            .statusCode(200)
            .body("email", is(updatedEmployeeDTO.email()))
            .body("fullName", is(updatedEmployeeDTO.fullName()))
            .body("birthday", is(updatedEmployeeDTO.birthday().toString()))
            .body("hobbies", is(updatedEmployeeDTO.hobbies()));
    }

    @Test
    void testUpdateEmployee_NonExistentEmployee() {
        var nonExistentId = UUID.randomUUID();
        var updatedEmployeeDTO = new EmployeeDTO(
            nonExistentId, "updated@sample.com", "Updated Name",
            LocalDate.of(2000, 1, 1), List.of("reading", "travelling")
        );

        given().auth().basic("admin", "admin")
            .contentType(ContentType.JSON)
            .body(updatedEmployeeDTO)
            .when()
            .put("/api/employees/" + nonExistentId)
            .then()
            .statusCode(404)  // assuming you return a 404 for non-existent resources
            .body("message", is(String.format("Employee with ID %s not found.", nonExistentId)))
            .body("errorId", notNullValue())
            .body("path", is("/api/employees/" + nonExistentId));
    }

    @Test
    void testUpdateEmployee_DuplicateEmail() {
        var initialEmployeeDTO = new EmployeeDTO(
            null, "initial@sample.com", "Initial Name",
            LocalDate.of(1990, 1, 1), List.of("hiking", "reading")
        );

        var anotherEmployeeDTO = new EmployeeDTO(
            null, "another@sample.com", "Another Name",
            LocalDate.of(1991, 1, 1), List.of("swimming", "writing")
        );

        UUID initialId = given().auth().basic("admin", "admin")
            .contentType(ContentType.JSON)
            .body(initialEmployeeDTO)
            .when()
            .post("/api/employees")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getObject("id", UUID.class);

        given().auth().basic("admin", "admin")
            .contentType(ContentType.JSON)
            .body(anotherEmployeeDTO)
            .when()
            .post("/api/employees")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getObject("id", UUID.class);


        var updatedEmployeeDTO = new EmployeeDTO(
            initialId, "another@sample.com", "Updated Name",
            LocalDate.of(2000, 1, 1), List.of("reading", "travelling")
        );

        given().auth().basic("admin", "admin")
            .contentType(ContentType.JSON)
            .body(updatedEmployeeDTO)
            .when()
            .put("/api/employees/" + initialId)
            .then()
            .statusCode(400)
            .body("message", is("Email another@sample.com already exists for a different employee!"));
    }

    @Test
    void testGetAllEmployees() {
        var employeeDTO = new EmployeeDTO(
            null, "testemail@sample.com", "Sample Name",
            LocalDate.of(1990, 1, 1), List.of("hiking", "reading")
        );

        given().auth().basic("admin", "admin")
            .contentType(ContentType.JSON)
            .body(employeeDTO)
            .when()
            .post("/api/employees")
            .then()
            .statusCode(200);

        given()
            .get("/api/employees")
            .then()
            .statusCode(200)
            .body("$", hasSize(greaterThan(0))); // at least 0 employees
    }

    @Test
    void testGetEmployeeById() {
        var employeeDTO = new EmployeeDTO(
            null, "testemail@sample.com", "Sample Name",
            LocalDate.of(1990, 1, 1), List.of("hiking", "reading")
        );

        var id = given().auth().basic("admin", "admin")
            .contentType(ContentType.JSON)
            .body(employeeDTO)
            .when()
            .post("/api/employees")
            .then()
            .statusCode(200)
            .extract()
            .path("id");

        given()
            .get("/api/employees/{id}", id)
            .then()
            .statusCode(200)
            .body("email", is(employeeDTO.email()))
            .body("fullName", is(employeeDTO.fullName()))
            .body("birthday", is(employeeDTO.birthday().toString()))
            .body("hobbies", is(employeeDTO.hobbies()));
    }

    @Test
    void testDeleteEmployeeById() {
        var employeeDTO = new EmployeeDTO(
            null, "testemail@sample.com", "Sample Name",
            LocalDate.of(1990, 1, 1), List.of("hiking", "reading")
        );

        var id = given().auth().basic("admin", "admin")
            .contentType(ContentType.JSON)
            .body(employeeDTO)
            .when()
            .post("/api/employees")
            .then()
            .statusCode(200)
            .extract()
            .path("id");

        given().auth().basic("admin", "admin")
            .delete("/api/employees/{id}", id)
            .then()
            .statusCode(204);
    }


    @ParameterizedTest
    @MethodSource("employeeDataProvider")
    void testCreateEmployee_ValidationErrors(EmployeeDTO employeeDTO, String expectedMessage) {
        given().auth().basic("admin", "admin")
            .contentType(ContentType.JSON)
            .body(employeeDTO)
            .when()
            .post("/api/employees")
            .then()
            .statusCode(400)
            .body("message", is(expectedMessage))
            .body("errorId", notNullValue())
            .body("path", is("/api/employees"));
    }

    static Stream<Arguments> employeeDataProvider() {
        return Stream.of(
            Arguments.of(
                new EmployeeDTO(
                    null, "testemail@sample.com", "Sample Name",
                    LocalDate.of(2300, 1, 1), List.of("hiking", "reading")
                ),
                "Birthday must be in the past"
            ),
            Arguments.of(
                new EmployeeDTO(
                    null, "wrongemail1231", "Sample Name",
                    LocalDate.of(1995, 1, 1), List.of("hiking", "reading")
                ),
                "Invalid email format"
            ),
            Arguments.of(
                new EmployeeDTO(
                    null, "testemail@sample.com", "",
                    LocalDate.of(1995, 1, 1), List.of("hiking", "reading")
                ),
                "Full name is required"
            )
        );
    }
}
