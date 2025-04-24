package org.amir.leavemanagementsystem.service;

import org.amir.leavemanagementsystem.model.Department;

import java.util.List;

public interface DepartmentService {
    Department createDepartment(Department department);
    Department getDepartment(Long id);
    Department getDepartment(String departmentName);
    List<Department> getAllDepartments();
    Department updateDepartment(Long id, Department department);
    void deleteDepartment(Long id);
    Department getDepartmentByName(String name);
    void ensureAdministrationDepartmentExists();
} 