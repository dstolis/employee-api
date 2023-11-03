/*
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright by KNIME AG, Zurich, Switzerland
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.com
 * email: contact@knime.com
 * ---------------------------------------------------------------------
 *
 * History
 *    Created on 2023-10-31 by dstolis
 */

package com.dstolis.employeeapi.model.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.dstolis.employeeapi.model.entity.Employee;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record EmployeeDTO(
    @Schema(description = "Unique identifier for the employee", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    UUID id,

    @Schema(description = "Email of the employee", example = "employee@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @Schema(description = "Full name of the employee", example = "John Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Full name is required")
    String fullName,

    @Schema(description = "Birthday of the employee", example = "1990-01-01")
    @Past(message = "Birthday must be in the past")
    @NotNull(message = "Birthday is required")
    LocalDate birthday,

    @Schema(description = "List of hobbies of the employee", example = "['Reading', 'Hiking']")
    List<String> hobbies) {

    public EmployeeDTO(final Employee employee) {
        this(employee.getId(), employee.getEmail(), employee.getFullName(), employee.getBirthday(),
            employee.getHobbies());
    }
}
