package com.dstolis.employeeapi.controller.handler;


import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.dstolis.employeeapi.model.dto.ErrorResponseDTO;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class EmployeeErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleJSONValidationException(MethodArgumentNotValidException ex,
        HttpServletRequest request) {
        var errorMessage = ex.getBindingResult().getAllErrors().stream()
            .map(ObjectError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        var error = new ErrorResponseDTO(OffsetDateTime.now(), errorMessage, UUID.randomUUID(),
            request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleEntityNotFoundException(EntityNotFoundException ex,
        HttpServletRequest request) {
        var error =
            new ErrorResponseDTO(OffsetDateTime.now(), ex.getMessage(), UUID.randomUUID(), request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleEntityExistsException(EntityExistsException ex,
        HttpServletRequest request) {
        var error =
            new ErrorResponseDTO(OffsetDateTime.now(), ex.getMessage(), UUID.randomUUID(), request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
