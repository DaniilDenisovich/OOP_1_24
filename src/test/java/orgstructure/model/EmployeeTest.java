package orgstructure.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import orgstructure.enums.Role;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для класса Employee.
 * Проверяют конструктор, геттеры, equals/hashCode и граничные случаи.
 */
class EmployeeTest {

    @Test
    @DisplayName("Создание сотрудника с корректными данными")
    void testValidEmployee() {
        Employee emp = new Employee("Иванов И.И.", Role.ENGINEER);
        assertEquals("Иванов И.И.", emp.getFullName());
        assertEquals(Role.ENGINEER, emp.getRole());
        assertNull(emp.getDepartment());
    }

    @Test
    @DisplayName("Создание сотрудника с null ФИО должно выбросить исключение")
    void testNullFullName() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Employee(null, Role.ENGINEER);
        });
    }

    @Test
    @DisplayName("Создание сотрудника с пустым ФИО должно выбросить исключение")
    void testEmptyFullName() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Employee("", Role.ENGINEER);
        });
    }

    @Test
    @DisplayName("Создание сотрудника с null ролью должно выбросить исключение")
    void testNullRole() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Employee("Иванов И.И.", null);
        });
    }

    @Test
    @DisplayName("Равенство сотрудников с одинаковыми ФИО и ролью")
    void testEqualsSameData() {
        Employee emp1 = new Employee("Иванов И.И.", Role.ENGINEER);
        Employee emp2 = new Employee("Иванов И.И.", Role.ENGINEER);
        assertEquals(emp1, emp2);
        assertEquals(emp1.hashCode(), emp2.hashCode());
    }

    @Test
    @DisplayName("Неравенство сотрудников с разными ФИО")
    void testNotEqualsDifferentName() {
        Employee emp1 = new Employee("Иванов И.И.", Role.ENGINEER);
        Employee emp2 = new Employee("Петров П.П.", Role.ENGINEER);
        assertNotEquals(emp1, emp2);
    }

    @Test
    @DisplayName("Неравенство сотрудников с разными ролями")
    void testNotEqualsDifferentRole() {
        Employee emp1 = new Employee("Иванов И.И.", Role.ENGINEER);
        Employee emp2 = new Employee("Иванов И.И.", Role.MANAGER);
        assertNotEquals(emp1, emp2);
    }

    @Test
    @DisplayName("Сотрудник не равен null")
    void testNotEqualsNull() {
        Employee emp = new Employee("Иванов И.И.", Role.ENGINEER);
        assertNotEquals(null, emp);
    }

    @Test
    @DisplayName("toString возвращает ФИО и роль")
    void testToString() {
        Employee emp = new Employee("Иванов И.И.", Role.ENGINEER);
        String result = emp.toString();
        assertTrue(result.contains("Иванов И.И."));
        assertTrue(result.contains("Инженер"));
    }
}
