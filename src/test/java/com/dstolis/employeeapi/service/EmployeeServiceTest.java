package com.dstolis.employeeapi.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dstolis.employeeapi.model.dto.EmployeeDTO;
import com.dstolis.employeeapi.model.entity.Employee;
import com.dstolis.employeeapi.model.entity.OutboxEvent;
import com.dstolis.employeeapi.repository.EmployeeRepository;
import com.dstolis.employeeapi.repository.OutboxRepository;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @InjectMocks
    private EmployeeService employeeService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    OutboxRepository outboxRepository;

    @Captor
    private ArgumentCaptor<OutboxEvent> outboxEventCaptor;


    @Test
    void testGetEmployeeById() {
        // Given
        var uuid = UUID.randomUUID();

        var mockEmployee = createMockEmployee(uuid);

        when(employeeRepository.findById(uuid)).thenReturn(Optional.of(mockEmployee));

        // When
        var employeeDTO = employeeService.getEmployeeById(uuid);

        // Then
        verify(employeeRepository, times(1)).findById(uuid);

        assertEmployeeDTO(mockEmployee, employeeDTO);
    }

    @Test
    void testUpdateEmployee() {
        // Given
        var uuid = UUID.randomUUID();

        var updatedEmployeeDTO = new EmployeeDTO(
            uuid, "new_email@example.com", "Updated Name",
            LocalDate.of(1995, 5, 5), List.of("new_hobby")
        );

        var mockEmployee = createMockEmployee(uuid);

        when(employeeRepository.findById(uuid)).thenReturn(Optional.of(mockEmployee));
        when(employeeRepository.save(mockEmployee)).thenReturn(mockEmployee);

        // When
        var newEmployeeDTO = employeeService.updateEmployee(uuid, updatedEmployeeDTO);

        // Then
        verify(employeeRepository, times(1)).findById(uuid);
        verify(employeeRepository, times(1)).save(mockEmployee);
        verify(outboxRepository, times(1)).save(outboxEventCaptor.capture());

        OutboxEvent capturedEvent = outboxEventCaptor.getValue();
        assertEquals("Employee", capturedEvent.getAggregateType());
        assertEquals("UPDATED", capturedEvent.getEventType());
        assertEquals(OutboxEvent.Status.PENDING, capturedEvent.getStatus());

        assertEmployeeDTO(mockEmployee, newEmployeeDTO);
    }

    @Test
    void testCreateEmployee() {
        // Given
        var newEmployeeDTO = new EmployeeDTO(
            null, "test@example.com", "Jane Doe",
            LocalDate.parse("1992-03-14"), List.of("yoga", "football")
        );

        var mockEmployee = createMockEmployee(null);

        when(employeeRepository.save(mockEmployee)).thenReturn(mockEmployee);

        // When
        var createdEmployeeDTO = employeeService.createEmployee(newEmployeeDTO);

        // Then
        verify(employeeRepository, times(1)).save(mockEmployee);
        verify(outboxRepository, times(1)).save(outboxEventCaptor.capture());

        OutboxEvent capturedEvent = outboxEventCaptor.getValue();
        assertEquals("Employee", capturedEvent.getAggregateType());
        assertEquals("CREATED", capturedEvent.getEventType());
        assertEquals(OutboxEvent.Status.PENDING, capturedEvent.getStatus());

        assertEmployeeDTO(mockEmployee, createdEmployeeDTO);
    }

    @Test
    void testCreateEmployeeWithExistingEmail() {
        // Given
        var newEmployeeDTO = new EmployeeDTO(
            null, "existing_email@example.com", "Jane Doe",
            LocalDate.parse("1992-03-14"), List.of("yoga", "football")
        );

        var existingEmployee = createMockEmployee(UUID.randomUUID());
        existingEmployee.setEmail("existing_email@example.com");

        when(employeeRepository.findByEmail("existing_email@example.com")).thenReturn(Optional.of(existingEmployee));

        // When & Then
        assertThrows(EntityExistsException.class,
            () -> employeeService.createEmployee(newEmployeeDTO));

        verify(employeeRepository, times(1)).findByEmail("existing_email@example.com");
        verify(employeeRepository, never()).save(any(Employee.class));
        verify(outboxRepository, never()).save(any(OutboxEvent.class));
    }

    @Test
    void testGetAllEmployees() {
        // Given
        var mockEmployees = createMockEmployeeList();
        when(employeeRepository.findAll()).thenReturn(mockEmployees);

        // When
        var employeeDTOs = employeeService.getAllEmployees();

        // Then
        verify(employeeRepository, times(1)).findAll();

        assertEquals(mockEmployees.size(), employeeDTOs.size());
        for (int i = 0; i < mockEmployees.size(); i++) {
            assertEmployeeDTO(mockEmployees.get(i), employeeDTOs.get(i));
        }
    }

    @Test
    void testUpdateEmployeeNotFound() {
        // Given
        var id = UUID.randomUUID();

        var updatedEmployeeDTO = new EmployeeDTO(
            id, "new_email@example.com", "Updated Name",
            LocalDate.of(1995, 5, 5), List.of("new_hobby")
        );

        var mockEmployee = createMockEmployee(id);

        when(employeeRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class,
            () -> employeeService.updateEmployee(id, updatedEmployeeDTO));

        verify(employeeRepository, times(1)).findById(id);
        verify(employeeRepository, never()).save(any(Employee.class));
        verify(outboxRepository, times(0)).save(any(OutboxEvent.class));
    }

    @Test
    void testGetEmployeeByIdNotFound() {
        // Given
        var id = UUID.randomUUID();

        when(employeeRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class,
            () -> employeeService.getEmployeeById(id));

        verify(employeeRepository, times(1)).findById(id);
    }

    @Test
    void testDeleteEmployeeSuccessful() {
        // Given
        var id = UUID.randomUUID();
        var mockEmployee = createMockEmployee(id);

        when(employeeRepository.findById(id)).thenReturn(Optional.of(mockEmployee));

        // When
        employeeService.deleteEmployee(id);

        // Then
        verify(employeeRepository, times(1)).findById(id);
        verify(employeeRepository, times(1)).delete(mockEmployee);
        verify(outboxRepository, times(1)).save(outboxEventCaptor.capture());

        OutboxEvent capturedEvent = outboxEventCaptor.getValue();
        assertEquals("Employee", capturedEvent.getAggregateType());
        assertEquals("DELETED", capturedEvent.getEventType());
        assertEquals(OutboxEvent.Status.PENDING, capturedEvent.getStatus());
    }

    @Test
    void testDeleteEmployeeNotFound() {
        // Given
        var uuid = UUID.randomUUID();

        when(employeeRepository.findById(uuid)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class,
            () -> employeeService.deleteEmployee(uuid));

        verify(employeeRepository, times(1)).findById(uuid);
        verify(employeeRepository, never()).delete(any(Employee.class));
        verify(outboxRepository, never()).delete(any(OutboxEvent.class));
    }

    private Employee createMockEmployee(UUID uuid) {
        var mockEmployee = new Employee();
        mockEmployee.setId(uuid);
        mockEmployee.setHobbies(List.of("yoga", "football"));
        mockEmployee.setEmail("test@example.com");
        mockEmployee.setFullName("Jane Doe");
        mockEmployee.setBirthday(LocalDate.parse("1992-03-14"));
        return mockEmployee;
    }

    private List<Employee> createMockEmployeeList() {
        List<Employee> employees = new ArrayList<>();
        employees.add(createMockEmployee(UUID.randomUUID()));
        employees.add(createMockEmployee(UUID.randomUUID()));
        employees.add(createMockEmployee(UUID.randomUUID()));
        return employees;
    }

    private void assertEmployeeDTO(Employee expectedEmployee, EmployeeDTO actualEmployeeDTO) {
        assertEquals(expectedEmployee.getId(), actualEmployeeDTO.id());
        assertEquals(expectedEmployee.getEmail(), actualEmployeeDTO.email());
        assertEquals(expectedEmployee.getFullName(), actualEmployeeDTO.fullName());
        assertEquals(expectedEmployee.getBirthday(), actualEmployeeDTO.birthday());
        assertEquals(expectedEmployee.getHobbies(), actualEmployeeDTO.hobbies());
    }

}
