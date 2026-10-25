package orgstructure.tests;

import orgstructure.enums.Role;
import orgstructure.model.Department;
import orgstructure.model.Employee;
import orgstructure.model.Document;
import orgstructure.model.ApprovalRecord;
import orgstructure.enums.StepStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты контрактов моделей, инкапсуляции и обхода дерева")
class ModelContractTest {

    @Test
    @DisplayName("Контракт equals и hashCode для Employee по ID")
    void testEmployeeEqualsAndHashCode() {
        Employee emp1 = new Employee("EMP-01", "Иван Иванов", Role.DEVELOPER);
        Employee emp2 = new Employee("EMP-01", "Иван И. Иванов", Role.TEAM_LEAD);
        Employee emp3 = new Employee("EMP-02", "Петр Петров", Role.DEVELOPER);

        assertEquals(emp1, emp2, "Сотрудники с одинаковым ID обязаны быть равны");
        assertEquals(emp1.hashCode(), emp2.hashCode(), "Хэш-коды при равенстве объектов должны совпадать");
        assertNotEquals(emp1, emp3, "Сотрудники с разными ID не должны быть равны");
        assertNotEquals(emp1, null, "Сравнение с null должно возвращать false");
    }

    @Test
    @DisplayName("Защита инкапсуляции списков Department (нельзя изменить извне)")
    void testDepartmentEncapsulation() {
        Department dept = new Department("D-01", "IT Отдел");
        Employee emp = new Employee("EMP-01", "Иван", Role.DEVELOPER);

        List<Employee> list = dept.getEmployees();
        assertThrows(UnsupportedOperationException.class, () -> list.add(emp),
                "Прямая модификация возвращаемого списка сотрудников должна быть запрещена");
    }

    @Test
    @DisplayName("Поиск руководителя на N уровней вверх: корректные уровни и выход за границы корня")
    void testFindManagerLevelsUp() {
        Department company = new Department("ROOT", "Главный офис");
        Employee ceo = new Employee("CEO", "Генеральный Директор", Role.DIRECTOR);
        company.setHead(ceo);

        Department itDept = new Department("IT", "Департамент IT");
        Employee cto = new Employee("CTO", "Технический Директор", Role.DEPARTMENT_HEAD);
        itDept.setHead(cto);
        company.addSubDepartment(itDept);

        Department devTeam = new Department("DEV", "Команда разработки");
        itDept.addSubDepartment(devTeam);

        assertEquals(Optional.empty(), devTeam.findManagerLevelsUp(0));

        assertEquals(Optional.of(cto), devTeam.findManagerLevelsUp(1));

        assertEquals(Optional.of(ceo), devTeam.findManagerLevelsUp(2));

        assertEquals(Optional.empty(), devTeam.findManagerLevelsUp(3));

        assertThrows(IllegalArgumentException.class, () -> devTeam.findManagerLevelsUp(-1));
    }

    @Test
    @DisplayName("Сбор всех подчинённых вглубь по всему дереву (DFS)")
    void testGetAllSubordinatesDeep() {
        Department root = new Department("ROOT", "Компания");
        Employee emp1 = new Employee("E1", "Сотрудник 1", Role.DIRECTOR);
        root.addEmployee(emp1);

        Department branchA = new Department("A", "Отдел A");
        Employee emp2 = new Employee("E2", "Сотрудник 2", Role.HR);
        branchA.addEmployee(emp2);
        root.addSubDepartment(branchA);

        Department subBranchA1 = new Department("A1", "Подотдел A1");
        Employee emp3 = new Employee("E3", "Сотрудник 3", Role.DEVELOPER);
        subBranchA1.addEmployee(emp3);
        branchA.addSubDepartment(subBranchA1);

        List<Employee> allEmployees = root.getAllSubordinatesDeep();
        assertEquals(3, allEmployees.size(), "Должны быть собраны все 3 сотрудника со всех уровней");
        assertTrue(allEmployees.containsAll(List.of(emp1, emp2, emp3)));
    }

    @Test
    @DisplayName("Проверка контракта компактного конструктора ApprovalRecord (record)")
    void testApprovalRecordContract() {
        Employee emp = new Employee("E1", "Согласующий", Role.DIRECTOR);
        LocalDateTime now = LocalDateTime.now();

        ApprovalRecord record = new ApprovalRecord(emp, StepStatus.APPROVED, null, now);
        assertEquals("", record.comment(), "Null-комментарий должен автоматически заменяться на пустую строку");

        assertThrows(NullPointerException.class,
                () -> new ApprovalRecord(null, StepStatus.APPROVED, "тест", now),
                "Approver не может быть null");
    }
}