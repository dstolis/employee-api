package com.dstolis.employeeapi.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dstolis.employeeapi.model.dto.EmployeeDTO;
import com.dstolis.employeeapi.model.dto.ErrorResponseDTO;
import com.dstolis.employeeapi.service.EmployeeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employee Management", description = "The Employee Management API provides endpoints for CRUD operations on employees. It allows clients to add, retrieve, update, and delete employee records. Each operation is secured and requires proper authorization.")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(final EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Operation(
        summary = "Create a new employee",
        description = "Creates a new employee record in the system. The provided employee data must be valid and adhere to the system's requirements.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Employee created successfully",
                content = { @Content(mediaType = "application/json",
                    schema = @Schema(implementation = EmployeeDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponseDTO.class))),
        },
        tags = { "Employee Management" }
    )
    @PostMapping
    public ResponseEntity<EmployeeDTO> createEmployee(@RequestBody @Valid EmployeeDTO employee) {
        var createdEmployee = employeeService.createEmployee(employee);
        return ResponseEntity.ok(createdEmployee);
    }

    @Operation(
        summary = "Get all employees",
        description = "Retrieves a list of all employees in the system. Can be used to populate employee management views.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of employees",
                content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = EmployeeDTO.class))))
        },
        tags = { "Employee Management" }
    )
    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        var employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @Operation(
        summary = "Get an employee by ID",
        description = "Retrieves a single employee record by their unique identifier. Useful for editing employee details or querying their data.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the employee",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = EmployeeDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid UUID format",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Employee not found",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponseDTO.class)))
        },
        tags = { "Employee Management" }
    )
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable UUID id) {
        var employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    @Operation(
        summary = "Update an existing employee by ID",
        description = "Updates the details of an existing employee. The employee to be updated is identified by their unique ID, and the updated employee data must be valid.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Successfully updated the employee",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = EmployeeDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data or invalid employee ID format",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Employee not found",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponseDTO.class)))
        },
        tags = { "Employee Management" }
    )
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable UUID id, @RequestBody @Valid EmployeeDTO employee) {
        var updatedEmployee = employeeService.updateEmployee(id, employee);
        return ResponseEntity.ok(updatedEmployee);
    }

    @Operation(
        summary = "Delete an employee by ID",
        description = "Removes an employee record from the system based on the provided unique identifier. This action is irreversible.",
        responses = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted the employee, no content to return"),
            @ApiResponse(responseCode = "400", description = "Invalid employee ID format",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Employee not found",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponseDTO.class)))
        },
        tags = { "Employee Management" }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable UUID id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
