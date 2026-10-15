package orgstructure.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Подразделение организации.
 * Формирует дерево: каждое подразделение может иметь дочерние подразделения и ссылку на родительское.
 */
public class Department {
    private final String name;
    private Employee head;
    private final List<Employee> employees;
    private final List<Department> children;
    private Department parent;

    /**
     * Создаёт подразделение с указанным названием.
     * @param name название подразделения (не должно быть null или пустым)
     * @throws IllegalArgumentException если name null или пустой
     */
    public Department(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Название подразделения не может быть пустым");
        }
        this.name = name;
        this.employees = new ArrayList<>();
        this.children = new ArrayList<>();
    }

    /** @return название подразделения */
    public String getName() {
        return name;
    }

    /** @return руководитель или null, если не назначен */
    public Employee getHead() {
        return head;
    }

    /**
     * Назначает руководителя подразделения.
     * @param head сотрудник, который становится руководителем
     */
    public void setHead(Employee head) {
        this.head = head;
    }

    /**
     * Возвращает список сотрудников подразделения.
     * @return неизменяемая копия списка для сохранения инкапсуляции
     */
    public List<Employee> getEmployees() {
        return Collections.unmodifiableList(employees);
    }

    /**
     * Добавляет сотрудника в подразделение.
     * @param employee сотрудник для добавления
     */
    public void addEmployee(Employee employee) {
        employees.add(employee);
    }

    /**
     * Возвращает список дочерних подразделений.
     * @return неизменяемая копия списка для сохранения инкапсуляции
     */
    public List<Department> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * Добавляет дочернее подразделение и автоматически устанавливает ссылку на родителя.
     * @param child дочернее подразделение
     */
    public void addChild(Department child) {
        children.add(child);
        child.parent = this;
    }

    /** @return родительское подразделение или null для корневого */
    public Department getParent() {
        return parent;
    }

    /**
     * Обходит дерево подразделений в глубину и собирает все подразделения.
     * @return список всех подразделений в дереве
     */
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

    /**
     * Находит руководителя на N уровней вверх от текущего подразделения.
     * @param levelsUp количество уровней для подъёма (1 = непосредственный руководитель)
     * @return руководитель или null, если достигли корня и выше нет
     * @throws IllegalArgumentException если levelsUp <= 0
     */
    public Employee findManagerNLevelsUp(int levelsUp) {
        if (levelsUp <= 0) {
            throw new IllegalArgumentException("levelsUp должен быть больше 0, передано: " + levelsUp);
        }
        Department current = this;
        for (int i = 0; i < levelsUp; i++) {
            if (current.parent == null) {
                return null;
            }
            current = current.parent;
        }
        return current.head;
    }

    /**
     * Собирает всех подчинённых вглубь дерева.
     * @return список всех сотрудников во всех дочерних подразделениях
     */
    public List<Employee> getAllSubordinates() {
        List<Employee> result = new ArrayList<>();
        collectSubordinates(this, result);
        return result;
    }

    private void collectSubordinates(Department dept, List<Employee> result) {
        for (Department child : dept.children) {
            if (child.head != null) {
                result.add(child.head);
            }
            result.addAll(child.employees);
            collectSubordinates(child, result);
        }
    }

    /**
     * Проверяет равенство подразделений по названию.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Department that = (Department) o;
        return Objects.equals(name, that.name);
    }

    /**
     * Вычисляет хеш-код на основе названия.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name + (head != null ? " [рук: " + head.getFullName() + "]" : "");
    }
}