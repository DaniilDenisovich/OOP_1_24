package orgstructure.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Department {

    private final String id;
    private final String name;
    private Department parent;
    private Employee head;

    private final List<Department> subDepartments = new ArrayList<>();
    private final List<Employee> employees = new ArrayList<>();

    public Department(String id, String name) {
        this.id = Objects.requireNonNull(id, "ID подразделения не может быть null");
        this.name = Objects.requireNonNull(name, "Название подразделения не может быть null");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Department getParent() {
        return parent;
    }

    private void setParent(Department parent) {
        this.parent = parent;
    }

    public Employee getHead() {
        return head;
    }

    public void setHead(Employee head) {
        this.head = head;
        if (head != null && !employees.contains(head)) {
            addEmployee(head);
        }
    }


    public void addSubDepartment(Department subDepartment) {
        Objects.requireNonNull(subDepartment, "Дочернее подразделение не может быть null");
        if (!subDepartments.contains(subDepartment)) {
            subDepartments.add(subDepartment);
            subDepartment.setParent(this);
        }
    }


    public void addEmployee(Employee employee) {
        Objects.requireNonNull(employee, "Сотрудник не может быть null");
        if (!employees.contains(employee)) {
            employees.add(employee);
            employee.setDepartment(this);
        }
    }

    public List<Department> getSubDepartments() {
        return Collections.unmodifiableList(subDepartments);
    }


    public List<Employee> getEmployees() {
        return Collections.unmodifiableList(employees);
    }


    public Optional<Employee> findManagerLevelsUp(int levelsUp) {
        if (levelsUp < 0) {
            throw new IllegalArgumentException("Число уровней вверх не может быть отрицательным: " + levelsUp);
        }

        Department current = this;
        for (int i = 0; i < levelsUp; i++) {
            if (current.getParent() == null) {
                return Optional.empty(); // Достигли вершины дерева раньше, чем поднялись на N уровней
            }
            current = current.getParent();
        }

        return Optional.ofNullable(current.getHead());
    }

    public List<Employee> getAllSubordinatesDeep() {
        List<Employee> allEmployees = new ArrayList<>();
        collectEmployeesRecursive(this, allEmployees);
        return Collections.unmodifiableList(allEmployees);
    }

    private void collectEmployeesRecursive(Department current, List<Employee> accumulator) {
        accumulator.addAll(current.employees);
        for (Department child : current.subDepartments) {
            collectEmployeesRecursive(child, accumulator);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Department that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Отдел: " + name + " (ID: " + id + ")";
    }
    /**
     * Выводит в консоль наглядное дерево подразделения с вложенными отделами,
     * руководителем и штатом сотрудников.
     *
     * @param indent текущий отступ для формирования древовидной графики
     */
    public void printHierarchy(String indent) {
        String headInfo = (head != null) ? " [Руководитель: " + head.getFullName() + "]" : " [Без руководителя]";
        System.out.println(indent + "└── 📁 " + name + " (ID: " + id + ")" + headInfo);

        // Печатаем сотрудников текущего отдела
        for (Employee emp : employees) {
            boolean isHead = emp.equals(head);
            System.out.println(indent + "    ├── 👤 " + emp.getFullName() + " — " + emp.getRole().getTitle()
                    + (isHead ? " ★ (Руководитель)" : ""));
        }

        // Рекурсивно спускаемся во все дочерние отделы
        for (Department sub : subDepartments) {
            sub.printHierarchy(indent + "    ");
        }
    }
}
