package orgstructure.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import orgstructure.enums.Role;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class DepartmentTest {

    @Test
    @DisplayName("Создание подразделения с корректным названием")
    void testValidDepartment() {
        Department dept = new Department("IT отдел");
        assertEquals("IT отдел", dept.getName());
        assertNull(dept.getHead());
        assertTrue(dept.getEmployees().isEmpty());
        assertTrue(dept.getChildren().isEmpty());
        assertNull(dept.getParent());
    }

    @Test
    @DisplayName("Создание подразделения с null названием должно выбросить исключение")
    void testNullName() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Department(null);
        });
    }

    @Test
    @DisplayName("Создание подразделения с пустым названием должно выбросить исключение")
    void testEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Department("   ");
        });
    }

    @Test
    @DisplayName("Создание подразделения с levelsUp <= 0 должно выбросить исключение")
    void testFindManagerInvalidLevelsUp() {
        Department dept = new Department("IT отдел");
        assertThrows(IllegalArgumentException.class, () -> {
            dept.findManagerNLevelsUp(0);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            dept.findManagerNLevelsUp(-1);
        });
    }

    @Test
    @DisplayName("Добавление сотрудника в подразделение")
    void testAddEmployee() {
        Department dept = new Department("IT отдел");
        Employee emp = new Employee("Иванов И.И.", Role.ENGINEER);
        dept.addEmployee(emp);
        assertEquals(1, dept.getEmployees().size());
        assertTrue(dept.getEmployees().contains(emp));
    }

    @Test
    @DisplayName("Возвращаемый список сотрудников неизменяем (Инкапсуляция)")
    void testUnmodifiableEmployeesList() {
        Department dept = new Department("IT отдел");
        Employee emp = new Employee("Иванов И.И.", Role.ENGINEER);
        dept.addEmployee(emp);

        List<Employee> employees = dept.getEmployees();
        assertThrows(UnsupportedOperationException.class, () -> {
            employees.add(new Employee("Петров П.П.", Role.MANAGER));
        });
    }

    @Test
    @DisplayName("Добавление дочернего подразделения устанавливает связь с родителем")
    void testAddChild() {
        Department parent = new Department("Компания");
        Department child = new Department("IT отдел");
        parent.addChild(child);

        assertEquals(1, parent.getChildren().size());
        assertTrue(parent.getChildren().contains(child));
        assertEquals(parent, child.getParent());
    }

    @Test
    @DisplayName("Возвращаемый список дочерних подразделений неизменяем (Инкапсуляция)")
    void testUnmodifiableChildrenList() {
        Department parent = new Department("Компания");
        Department child = new Department("IT отдел");
        parent.addChild(child);

        List<Department> children = parent.getChildren();
        assertThrows(UnsupportedOperationException.class, () -> {
            children.add(new Department("Бухгалтерия"));
        });
    }

    @Test
    @DisplayName("Обход дерева подразделений в глубину")
    void testGetAllDepartments() {
        Department root = new Department("Компания");
        Department child1 = new Department("IT");
        Department child2 = new Department("Бухгалтерия");
        Department grandChild = new Department("Разработка");

        root.addChild(child1);
        root.addChild(child2);
        child1.addChild(grandChild);

        List<Department> all = root.getAllDepartments();
        assertEquals(4, all.size());
        assertTrue(all.contains(root));
        assertTrue(all.contains(child1));
        assertTrue(all.contains(child2));
        assertTrue(all.contains(grandChild));
    }

    @Test
    @DisplayName("Поиск руководителя на N уровней вверх")
    void testFindManagerNLevelsUp() {
        Department root = new Department("Компания");
        Employee director = new Employee("Директор", Role.DIRECTOR);
        root.setHead(director);

        Department child = new Department("IT");
        Employee itHead = new Employee("IT Руководитель", Role.DEPARTMENT_HEAD);
        child.setHead(itHead);
        root.addChild(child);

        Department grandChild = new Department("Разработка");
        Employee devHead = new Employee("Разработчик Руководитель", Role.DEPARTMENT_HEAD);
        grandChild.setHead(devHead);
        child.addChild(grandChild);

        // На 1 уровень вверх от Разработка → IT Руководитель
        Employee manager1 = grandChild.findManagerNLevelsUp(1);
        assertEquals(itHead, manager1);

        // На 2 уровня вверх от Разработка → Директор
        Employee manager2 = grandChild.findManagerNLevelsUp(2);
        assertEquals(director, manager2);

        // На 3 уровня вверх от Разработка → null (выше корня некуда идти)
        Employee manager3 = grandChild.findManagerNLevelsUp(3);
        assertNull(manager3);

        // На 10 уровней вверх → тоже null
        Employee manager10 = grandChild.findManagerNLevelsUp(10);
        assertNull(manager10);
    }

    @Test
    @DisplayName("Поиск руководителя на 1 уровень вверх от корневого подразделения → null")
    void testFindManagerFromRoot() {
        Department root = new Department("Компания");
        Employee director = new Employee("Директор", Role.DIRECTOR);
        root.setHead(director);

        Employee manager = root.findManagerNLevelsUp(1);
        assertNull(manager);
    }

    @Test
    @DisplayName("Поиск всех подчинённых вглубь дерева")
    void testGetAllSubordinates() {
        Department root = new Department("Компания");
        Employee rootHead = new Employee("Директор", Role.DIRECTOR);
        root.setHead(rootHead);

        Department child = new Department("IT");
        Employee itHead = new Employee("IT Руководитель", Role.DEPARTMENT_HEAD);
        child.setHead(itHead);
        Employee emp1 = new Employee("Инженер 1", Role.ENGINEER);
        child.addEmployee(emp1);
        root.addChild(child);

        List<Employee> subordinates = root.getAllSubordinates();
        assertEquals(2, subordinates.size());
        assertTrue(subordinates.contains(itHead));
        assertTrue(subordinates.contains(emp1));
    }

    @Test
    @DisplayName("Равенство подразделений по названию (контракт equals/hashCode)")
    void testEqualsAndHashCode() {
        Department dept1 = new Department("IT отдел");
        Department dept2 = new Department("IT отдел");

        assertEquals(dept1, dept2);
        assertEquals(dept1.hashCode(), dept2.hashCode());
    }

    @Test
    @DisplayName("Неравенство подразделений с разными названиями")
    void testNotEquals() {
        Department dept1 = new Department("IT отдел");
        Department dept2 = new Department("Бухгалтерия");
        assertNotEquals(dept1, dept2);
    }
}