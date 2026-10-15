package orgstructure.model;

import orgstructure.enums.Role;
import java.util.Objects;

/**
 * Сотрудник организации.
 * Имеет ФИО, роль и ссылку на подразделение.
 */
public class Employee {
    private final String fullName;
    private final Role role;
    private Department department;

    /**
     * Создаёт сотрудника с указанным ФИО и ролью.
     * @param fullName ФИО сотрудника (не должно быть null или пустым)
     * @param role роль сотрудника в организации
     * @throws IllegalArgumentException если fullName null или пустой, или role null
     */
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

    /** @return ФИО сотрудника */
    public String getFullName() {
        return fullName;
    }

    /** @return роль сотрудника */
    public Role getRole() {
        return role;
    }

    /** @return подразделение сотрудника или null, если не назначено */
    public Department getDepartment() {
        return department;
    }

    /**
     * Назначает сотрудника в подразделение.
     * @param department подразделение
     */
    public void setDepartment(Department department) {
        this.department = department;
    }

    /**
     * Проверяет равенство сотрудников по ФИО и роли.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return Objects.equals(fullName, employee.fullName) && role == employee.role;
    }

    /**
     * Вычисляет хеш-код на основе ФИО и роли.
     */
    @Override
    public int hashCode() {
        return Objects.hash(fullName, role);
    }

    @Override
    public String toString() {
        return fullName + " (" + role.getDisplayName() + ")";
    }
}