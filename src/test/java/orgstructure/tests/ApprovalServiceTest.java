package orgstructure.tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import orgstructure.enums.ApproverRule;
import orgstructure.enums.DocumentStatus;
import orgstructure.enums.Role;
import orgstructure.exception.ApprovalException;
import orgstructure.model.ApprovalStep;
import orgstructure.model.Department;
import orgstructure.model.Document;
import orgstructure.model.Employee;
import orgstructure.service.ApprovalService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class ApprovalServiceTest {

    private final ApprovalService service = new ApprovalService();

    @Test
    @DisplayName("Поиск руководителя на N уровней вверх")
    void testFindManagerNLevelsUp() {
        Department root = new Department("Компания");
        Employee director = new Employee("Директор", Role.DIRECTOR);
        root.setHead(director);
        director.setDepartment(root);

        Department child = new Department("Отдел");
        Employee deptHead = new Employee("Начальник отдела", Role.DEPARTMENT_HEAD);
        child.setHead(deptHead);
        deptHead.setDepartment(child);
        root.addChild(child);

        Department subChild = new Department("Группа");
        Employee teamLead = new Employee("Тимлид", Role.DEPARTMENT_HEAD);
        subChild.setHead(teamLead);
        teamLead.setDepartment(subChild);
        child.addChild(subChild);

        Employee manager1 = service.findManagerNLevelsUp(teamLead, 1);
        assertEquals("Начальник отдела", manager1.getFullName());

        Employee manager2 = service.findManagerNLevelsUp(teamLead, 2);
        assertEquals("Директор", manager2.getFullName());
    }

    @Test
    @DisplayName("Поиск всех подчинённых вглубь дерева")
    void testGetAllSubordinates() {
        Department root = new Department("Компания");
        Department child = new Department("Отдел");
        child.addEmployee(new Employee("Сотрудник 1", Role.ENGINEER));
        child.addEmployee(new Employee("Сотрудник 2", Role.ENGINEER));
        root.addChild(child);

        List<Employee> subs = service.getAllSubordinates(root);
        assertEquals(2, subs.size(), "Должны быть найдены все сотрудники дочерних подразделений");
    }

    @Test
    @DisplayName("Построение маршрута согласования")
    void testBuildRoute() {
        Document doc = new Document("Заявка");
        Department dept = new Department("IT");
        dept.setHead(new Employee("Начальник", Role.DEPARTMENT_HEAD));

        service.buildRoute(doc, dept, List.of(ApproverRule.DEPARTMENT_HEAD));

        assertEquals(1, doc.getSteps().size());
        assertNotNull(doc.getSteps().get(0).getExpectedApprover(), "Ожидаемый согласующий должен быть определён");
    }

    @Test
    @DisplayName("Успешное согласование документа")
    void testApproveStepSuccess() {
        Document doc = new Document("Заявка");
        Department dept = new Department("IT");
        Employee boss = new Employee("Начальник", Role.DEPARTMENT_HEAD);
        dept.setHead(boss);

        service.buildRoute(doc, dept, List.of(ApproverRule.DEPARTMENT_HEAD));
        doc.startApproval();

        ApprovalStep step = doc.getCurrentStep();
        service.approveStep(doc, step, boss);

        assertEquals(DocumentStatus.APPROVED, doc.getStatus(), "После согласования единственного шага документ должен быть согласован");
    }

    @Test
    @DisplayName("Успешное отклонение документа")
    void testRejectStepSuccess() {
        Document doc = new Document("Заявка");
        Department dept = new Department("IT");
        Employee boss = new Employee("Начальник", Role.DEPARTMENT_HEAD);
        dept.setHead(boss);

        service.buildRoute(doc, dept, List.of(ApproverRule.DEPARTMENT_HEAD));
        doc.startApproval();

        ApprovalStep step = doc.getCurrentStep();
        service.rejectStep(doc, step, boss);

        assertEquals(DocumentStatus.REJECTED, doc.getStatus(), "После отклонения шага документ должен быть отклонён");
    }

    @Test
    @DisplayName("Попытка согласовать чужой шаг выбрасывает исключение")
    void testApproveWrongPersonThrows() {
        Document doc = new Document("Заявка");
        Department dept = new Department("IT");
        Employee boss = new Employee("Начальник", Role.DEPARTMENT_HEAD);
        dept.setHead(boss);

        Employee randomEmployee = new Employee("Случайный сотрудник", Role.ENGINEER);

        service.buildRoute(doc, dept, List.of(ApproverRule.DEPARTMENT_HEAD));
        doc.startApproval();

        ApprovalStep step = doc.getCurrentStep();

        assertThrows(ApprovalException.class, () -> {
            service.approveStep(doc, step, randomEmployee);
        }, "Согласование не тем сотрудником должно выбрасывать ApprovalException");
    }
}