package com.dstolis.employeeapi.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dstolis.employeeapi.model.dto.EmployeeDTO;
import com.dstolis.employeeapi.model.dto.EmployeeEvent;
import com.dstolis.employeeapi.model.entity.Employee;
import com.dstolis.employeeapi.model.entity.OutboxEvent;
import com.dstolis.employeeapi.repository.EmployeeRepository;
import com.dstolis.employeeapi.repository.OutboxRepository;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    private final OutboxRepository outboxRepository;

    @Autowired
    public EmployeeService(final EmployeeRepository employeeRepository, final OutboxRepository outboxRepository) {
        this.employeeRepository = employeeRepository;
        this.outboxRepository = outboxRepository;
    }

    @Transactional(rollbackOn = Exception.class)
    public EmployeeDTO createEmployee(EmployeeDTO employeeDto) {
        // Check if employee with the same email already exists
        employeeRepository.findByEmail(employeeDto.email()).ifPresent(employee -> {
            throw new EntityExistsException("Email " + employeeDto.email() + " already exists!");
        });

        var employee = new Employee(employeeDto);
        var savedEmployee = employeeRepository.save(employee);


        var payload = new EmployeeEvent(savedEmployee.getId(), EmployeeEvent.EventType.CREATED).toString();
        var outboxEvent = new OutboxEvent(
            savedEmployee.getId(),
            "Employee",
            "CREATED",
            payload,
            OffsetDateTime.now(),
            OutboxEvent.Status.PENDING
        );
        outboxRepository.save(outboxEvent);

        return new EmployeeDTO(savedEmployee);
    }

    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll()
            .stream()
            .map(EmployeeDTO::new)
            .toList();
    }

    public EmployeeDTO getEmployeeById(final UUID uuid) {
        return employeeRepository.findById(uuid)
            .map(EmployeeDTO::new)
            .orElseThrow(() -> new EntityNotFoundException("Employee with ID " + uuid + " not found."));
    }

    @Transactional(rollbackOn = Exception.class)
    public EmployeeDTO updateEmployee(final UUID id, EmployeeDTO employeeDto) {
        employeeRepository.findByEmail(employeeDto.email()).ifPresent(employee -> {
            if (!employee.getId().equals(id)) {
                throw new EntityExistsException("Email " + employeeDto.email() + " already exists for a different employee!");
            }
        });
        var updatedEmployee = employeeRepository.findById(id)
            .map(existingEmployee -> {
                existingEmployee.setEmail(employeeDto.email());
                existingEmployee.setFullName(employeeDto.fullName());
                existingEmployee.setBirthday(employeeDto.birthday());
                existingEmployee.setHobbies(employeeDto.hobbies());
                return existingEmployee;
            })
            .orElseThrow(() -> new EntityNotFoundException("Employee with ID " + id + " not found."));

        var savedEmployee = employeeRepository.save(updatedEmployee);

        var payload = new EmployeeEvent(savedEmployee.getId(), EmployeeEvent.EventType.UPDATED).toString();
        var outboxEvent = new OutboxEvent(
            savedEmployee.getId(),
            "Employee",
            "UPDATED",
            payload,
            OffsetDateTime.now(),
            OutboxEvent.Status.PENDING
        );
        outboxRepository.save(outboxEvent);

        return new EmployeeDTO(savedEmployee);
    }

    @Transactional(rollbackOn = Exception.class)
    public void deleteEmployee(final UUID id) {
        var employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Employee with ID " + id + " not found."));
        employeeRepository.delete(employee);
        var payload = new EmployeeEvent(id, EmployeeEvent.EventType.DELETED).toString();
        var outboxEvent = new OutboxEvent(
            id,
            "Employee",
            "DELETED",
            payload,
            OffsetDateTime.now(),
            OutboxEvent.Status.PENDING
        );
        outboxRepository.save(outboxEvent);
    }
}
