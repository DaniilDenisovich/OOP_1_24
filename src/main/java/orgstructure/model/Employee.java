package orgstructure.model;

import orgstructure.enums.Role;

import java.util.Objects;


public class Employee {
    private final String fullName;
    private final Role role;
    private Department department;


    public Employee(String fullName, Role role) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("ФИО сотрудника не может быть пустым");
        }
        if (role == null) {
            throw new IllegalArgumentException("Роль сотрудника не может быть null");
        }
        this.fullName = fullName;
        this.role = role;
    }


    public String getFullName() {
        return fullName;
    }


    public Role getRole() {
        return role;
    }


    public Department getDepartment() {
        return department;
    }


    public void setDepartment(Department department) {
        this.department = department;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return Objects.equals(fullName, employee.fullName) && role == employee.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullName, role);
    }

    @Override
    public String toString() {
        return fullName + " (" + role.getDisplayName() + ")";
    }
}