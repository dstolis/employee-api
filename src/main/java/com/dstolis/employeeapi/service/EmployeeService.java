package com.dstolis.employeeapi.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.dstolis.employeeapi.model.dto.EmployeeDTO;
import com.dstolis.employeeapi.model.dto.EmployeeEvent;
import com.dstolis.employeeapi.model.entity.Employee;
import com.dstolis.employeeapi.repository.EmployeeRepository;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    private final KafkaTemplate<String, EmployeeEvent> kafkaTemplate;

    private static final String TOPIC = "employee-events";

    @Autowired
    public EmployeeService(final EmployeeRepository employeeRepository,
        final KafkaTemplate<String, EmployeeEvent> kafkaTemplate) {
        this.employeeRepository = employeeRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional(rollbackOn = Exception.class)
    public EmployeeDTO createEmployee(EmployeeDTO employeeDto) {
        employeeRepository.findByEmail(employeeDto.email()).ifPresent(employee -> {
            throw new EntityExistsException("Email " + employeeDto.email() + " already exists!");
        });
        var employee = new Employee(employeeDto);
        var savedEmployee = employeeRepository.save(employee);
        kafkaTemplate.send(TOPIC,
            new EmployeeEvent(savedEmployee.getId(), EmployeeEvent.EventType.CREATED));
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

        kafkaTemplate.send(TOPIC,
            new EmployeeEvent(savedEmployee.getId(), EmployeeEvent.EventType.UPDATED));

        return new EmployeeDTO(savedEmployee);
    }

    @Transactional(rollbackOn = Exception.class)
    public void deleteEmployee(final UUID id) {
        var employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Employee with ID " + id + " not found."));
        employeeRepository.delete(employee);
        kafkaTemplate.send(TOPIC, new EmployeeEvent(id, EmployeeEvent.EventType.DELETED));
    }
}
