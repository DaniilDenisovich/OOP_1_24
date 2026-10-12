package orgstructure.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class Department {
    private final String name;
    private Employee head;
    private final List<Employee> employees;
    private final List<Department> children;
    private Department parent;


    public Department(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Название подразделения не может быть пустым");
        }
        this.name = name;
        this.employees = new ArrayList<>();
        this.children = new ArrayList<>();
    }


    public String getName() {
        return name;
    }


    public Employee getHead() {
        return head;
    }


    public void setHead(Employee head) {
        this.head = head;
    }


    public List<Employee> getEmployees() {
        return Collections.unmodifiableList(employees);
    }


    public void addEmployee(Employee employee) {
        employees.add(employee);
    }

    public List<Department> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public void addChild(Department child) {
        children.add(child);
        child.parent = this;
    }


    public Department getParent() {
        return parent;
    }


    public List<Department> getAllDepartments() {
        List<Department> result = new ArrayList<>();
        collectDepartments(this, result);
        return result;
    }

    private void collectDepartments(Department dept, List<Department> result) {
        result.add(dept);
        for (Department child : dept.children) {
            collectDepartments(child, result);
        }
    }


    public Employee findManagerNLevelsUp(int levelsUp) {
        Department current = this;
        for (int i = 0; i < levelsUp && current.parent != null; i++) {
            current = current.parent;
        }
        return current.head;
    }

    public List<Employee> getAllSubordinates() {
        List<Employee> result = new ArrayList<>();
        collectSubordinates(this, result);
        return result;
    }

    private void collectSubordinates(Department dept, List<Employee> result) {
        for (Department child : dept.children) {
            result.addAll(child.employees);
            if (child.head != null) {
                result.add(child.head);
            }
            collectSubordinates(child, result);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Department that = (Department) o;
        return Objects.equals(name, that.name);
    }


    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name + (head != null ? " [рук: " + head.getFullName() + "]" : "");
    }
}