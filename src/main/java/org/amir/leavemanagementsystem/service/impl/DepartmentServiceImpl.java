package org.amir.leavemanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.amir.leavemanagementsystem.exception.ResourceNotFoundException;
import org.amir.leavemanagementsystem.model.Department;
import org.amir.leavemanagementsystem.repository.DepartmentRepository;
import org.amir.leavemanagementsystem.service.DepartmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public Department createDepartment(Department department) {
        return departmentRepository.save(department);
    }

    @Override
    public Department getDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    @Override
    public Department getDepartment(String departmentName) {
        return departmentRepository.findByName(departmentName)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with name: " + departmentName));
    }

    @Override
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Override
    @Transactional
    public Department updateDepartment(Long id, Department department) {
        Department existingDepartment = getDepartment(id);
        
        existingDepartment.setName(department.getName());
        existingDepartment.setDescription(department.getDescription());
        
        return departmentRepository.save(existingDepartment);
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        Department department = getDepartment(id);
        departmentRepository.delete(department);
    }

    @Override
    public Department getDepartmentByName(String name) {
        return departmentRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with name: " + name));
    }

    @Override
    @Transactional
    public void ensureAdministrationDepartmentExists() {
        if (!departmentRepository.existsByName("Administration")) {
            Department adminDepartment = Department.builder()
                    .name("Administration")
                    .description("Administration Department")
                    .build();
            departmentRepository.save(adminDepartment);
        }
    }
} 