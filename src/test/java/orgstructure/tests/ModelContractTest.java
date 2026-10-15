package orgstructure.tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import orgstructure.enums.ApproverRule;
import orgstructure.enums.DocumentStatus;
import orgstructure.enums.Role;
import orgstructure.enums.StepStatus;
import orgstructure.model.ApprovalStep;
import orgstructure.model.Department;
import orgstructure.model.Document;
import orgstructure.model.Employee;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class ModelContractTest {

    @Test
    @DisplayName("Контракт equals и hashCode для Employee")
    void testEmployeeEqualsAndHashCode() {
        Employee emp1 = new Employee("Иванов И.И.", Role.ENGINEER);
        Employee emp2 = new Employee("Иванов И.И.", Role.ENGINEER);
        Employee emp3 = new Employee("Петров П.П.", Role.ENGINEER);

        assertEquals(emp1, emp2, "Сотрудники с одинаковыми ФИО и ролью должны быть равны");
        assertEquals(emp1.hashCode(), emp2.hashCode(), "Хеш-коды равных объектов должны совпадать");
        assertNotEquals(emp1, emp3, "Сотрудники с разными ФИО не должны быть равны");
    }

    @Test
    @DisplayName("Граничный случай: создание Employee с некорректными данными")
    void testEmployeeInvalidDataThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Employee(null, Role.ENGINEER), "Должен выбрасываться при null имени");
        assertThrows(IllegalArgumentException.class, () -> new Employee("   ", Role.ENGINEER), "Должен выбрасываться при пустом имени");
        assertThrows(IllegalArgumentException.class, () -> new Employee("Иванов", null), "Должен выбрасываться при null роли");
    }

    @Test
    @DisplayName("Инкапсуляция: список сотрудников подразделения неизменяем")
    void testDepartmentEncapsulation() {
        Department dept = new Department("IT");
        dept.addEmployee(new Employee("Иванов", Role.ENGINEER));

        List<Employee> employees = dept.getEmployees();
        assertThrows(UnsupportedOperationException.class, () -> {
            employees.add(new Employee("Петров", Role.MANAGER));
        }, "Внешнее изменение возвращаемого списка должно быть запрещено");
    }

    @Test
    @DisplayName("Обход дерева: поиск всех подразделений")
    void testDepartmentTreeTraversal() {
        Department root = new Department("Компания");
        Department child = new Department("Отдел");
        root.addChild(child);

        List<Department> all = root.getAllDepartments();
        assertEquals(2, all.size(), "Должны быть найдены корень и дочерний элемент");
    }

    @Test
    @DisplayName("Жизненный цикл документа: переход в статус согласования")
    void testDocumentStatusTransition() {
        Document doc = new Document("Договор");
        doc.addStep(new ApprovalStep(ApproverRule.DIRECT_MANAGER, null));

        assertEquals(DocumentStatus.DRAFT, doc.getStatus());
        doc.startApproval();
        assertEquals(DocumentStatus.IN_APPROVAL, doc.getStatus(), "После запуска статус должен измениться");
    }

    @Test
    @DisplayName("Граничный случай: повторное согласование завершённого шага")
    void testApprovalStepDoubleActionThrows() {
        ApprovalStep step = new ApprovalStep(ApproverRule.DIRECT_MANAGER, null);
        Employee approver = new Employee("Иванов", Role.DIRECTOR);

        step.approve(approver);
        assertEquals(StepStatus.APPROVED, step.getStatus());

        assertThrows(IllegalStateException.class, () -> step.approve(approver),
                "Повторное согласование завершённого шага должно выбрасывать исключение");
    }
}