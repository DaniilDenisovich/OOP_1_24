package orgstructure.model;

import orgstructure.enums.Role;

import java.util.Objects;

public class Employee {

    private final String id;
    private final String fullName;
    private final Role role;
    private Department department;


    public Employee(String id, String fullName, Role role) {
        this.id = Objects.requireNonNull(id, "Идентификатор сотрудника не может быть null");
        this.fullName = Objects.requireNonNull(fullName, "ФИО не может быть null");
        this.role = Objects.requireNonNull(role, "Роль не может быть null");
    }

    public String getId() {
        return id;
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

    void setDepartment(Department department) {
        this.department = department;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee employee)) return false; // Использование pattern matching Java 21
        return Objects.equals(id, employee.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return fullName + " (" + role + ", ID: " + id + ")";
    }
}