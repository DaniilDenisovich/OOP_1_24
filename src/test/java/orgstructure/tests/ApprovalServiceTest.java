package orgstructure.tests;

import orgstructure.enums.DocumentStatus;
import orgstructure.enums.Role;
import orgstructure.enums.StepStatus;
import orgstructure.exception.ApprovalException;
import orgstructure.exception.UnauthorizedApproverException;
import orgstructure.model.ApprovalStep;
import orgstructure.model.Department;
import orgstructure.model.Document;
import orgstructure.model.Employee;
import orgstructure.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты бизнес-логики процесса согласования и обработки исключений")
class ApprovalServiceTest {

    private Department company;
    private Department devDept;
    private Employee author;
    private Employee lead;
    private Employee director;
    private Employee accountant;
    private ApprovalService approvalService;

    @BeforeEach
    void setUp() {
        company = new Department("ROOT", "Главная Компания");
        director = new Employee("DIR-01", "Ольга Директорова", Role.DIRECTOR);
        accountant = new Employee("ACC-01", "Мария Бухгалтер", Role.ACCOUNTANT);
        company.setHead(director);
        company.addEmployee(accountant);

        devDept = new Department("DEV-01", "Отдел Разработки");
        lead = new Employee("LEAD-01", "Алексей Тимлид", Role.TEAM_LEAD);
        author = new Employee("DEV-01", "Иван Разработчик", Role.DEVELOPER);
        devDept.setHead(lead);
        devDept.addEmployee(author);

        company.addSubDepartment(devDept);

        approvalService = new ApprovalService(company);
    }

    @Test
    @DisplayName("Успешное построение маршрута и полное согласование документа")
    void testSuccessfulFullApprovalProcess() throws ApprovalException {
        Document doc = new Document("DOC-101", "Заявка на отпуск", author, LocalDateTime.now());

        List<ApproverRule> rules = List.of(
                new DirectManagerRule(),
                new RoleHolderRule(Role.ACCOUNTANT),
                new DepartmentHeadRule(1) // Руководитель департамента выше (директор)
        );

        LocalDateTime now = LocalDateTime.now();
        approvalService.buildRoute(doc, rules, Duration.ofHours(24), now);

        assertEquals(DocumentStatus.IN_PROGRESS, doc.getStatus());
        assertEquals(3, doc.getRoute().size());

        approvalService.approve(doc, lead, "Согласовано тимлидом", now.plusHours(1));
        assertEquals(StepStatus.APPROVED, doc.getRoute().get(0).getStatus());
        assertEquals(DocumentStatus.IN_PROGRESS, doc.getStatus());

        approvalService.approve(doc, accountant, "Отпускные рассчитаны", now.plusHours(2));
        assertEquals(StepStatus.APPROVED, doc.getRoute().get(1).getStatus());

        approvalService.approve(doc, director, "Утверждаю", now.plusHours(3));
        assertEquals(StepStatus.APPROVED, doc.getRoute().get(2).getStatus());

        assertEquals(DocumentStatus.APPROVED, doc.getStatus());
        assertEquals(3, doc.getHistory().size());
        assertEquals("Утверждаю", doc.getHistory().get(2).comment());
    }

    @Test
    @DisplayName("Отклонение документа на первом шаге завершает маршрут со статусом REJECTED")
    void testRejectOnFirstStep() throws ApprovalException {
        Document doc = new Document("DOC-102", "Закупка оборудования", author, LocalDateTime.now());
        approvalService.buildRoute(doc, List.of(new DirectManagerRule()), Duration.ofHours(48), LocalDateTime.now());

        approvalService.reject(doc, lead, "Бюджет на текущий квартал исчерпан", LocalDateTime.now());

        assertEquals(DocumentStatus.REJECTED, doc.getStatus());
        assertEquals(StepStatus.REJECTED, doc.getRoute().get(0).getStatus());
        assertEquals(1, doc.getHistory().size());
        assertEquals("Бюджет на текущий квартал исчерпан", doc.getHistory().get(0).comment());
    }

    @Test
    @DisplayName("Выброс непроверяемого UnauthorizedApproverException при согласовании чужого шага")
    void testUnauthorizedApproverThrowsException() throws ApprovalException {
        Document doc = new Document("DOC-103", "Премия", author, LocalDateTime.now());
        approvalService.buildRoute(doc, List.of(new DirectManagerRule()), Duration.ofHours(24), LocalDateTime.now());

        UnauthorizedApproverException ex = assertThrows(
                UnauthorizedApproverException.class,
                () -> approvalService.approve(doc, director, "Я директор, согласую за всех", LocalDateTime.now())
        );

        assertTrue(ex.getMessage().contains("не имеет права согласовывать шаг"));
        assertEquals(StepStatus.PENDING, doc.getRoute().get(0).getStatus());
    }

    @Test
    @DisplayName("Выброс проверяемого ApprovalException при отсутствии подходящего согласующего по роли")
    void testMissingRoleThrowsCheckedApprovalException() {
        Document doc = new Document("DOC-104", "Договор", author, LocalDateTime.now());
        List<ApproverRule> rules = List.of(new RoleHolderRule(Role.LAWYER));

        ApprovalException ex = assertThrows(
                ApprovalException.class,
                () -> approvalService.buildRoute(doc, rules, Duration.ofHours(12), LocalDateTime.now())
        );

        assertTrue(ex.getMessage().contains("не найден сотрудник с ролью"),
                "Сообщение об ошибке должно сообщать об отсутствии сотрудника с заданной ролью");
    }

    @Test
    @DisplayName("Выброс проверяемого ApprovalException при невозможности подняться выше корня")
    void testRootDepartmentHeadHasNoManagerAbove() {
        Document doc = new Document("DOC-105", "Приказ о реорганизации", director, LocalDateTime.now());
        List<ApproverRule> rules = List.of(new DirectManagerRule());

        ApprovalException ex = assertThrows(
                ApprovalException.class,
                () -> approvalService.buildRoute(doc, rules, Duration.ofHours(12), LocalDateTime.now())
        );

        assertTrue(ex.getMessage().contains("нет вышестоящего руководителя"));
    }

    @Test
    @DisplayName("Выброс ApprovalException при попытке создать маршрут без правил")
    void testEmptyRulesThrowsCheckedException() {
        Document doc = new Document("DOC-106", "Записка", author, LocalDateTime.now());

        assertThrows(ApprovalException.class,
                () -> approvalService.buildRoute(doc, List.of(), Duration.ofHours(1), LocalDateTime.now()));
    }

    @Test
    @DisplayName("Проверка дедлайна шага согласования через Duration и проверку просрочки")
    void testStepDurationAndDeadline() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 15, 10, 0);
        Duration limit = Duration.ofHours(4);

        ApprovalStep step = new ApprovalStep(1, lead, limit, start);

        assertEquals(start.plusHours(4), step.getDeadline(), "Дедлайн должен быть ровно через 4 часа");
        assertFalse(step.isOverdue(start.plusHours(3)), "Спустя 3 часа шаг еще не должен быть просрочен");
        assertTrue(step.isOverdue(start.plusHours(5)), "Спустя 5 часов шаг должен определяться как просроченный");
    }

    @Test
    @DisplayName("Запрет на изменение маршрута у документа не в статусе DRAFT")
    void testCannotChangeRouteWhenNotInDraft() throws ApprovalException {
        Document doc = new Document("DOC-107", "Отчет", author, LocalDateTime.now());
        approvalService.buildRoute(doc, List.of(new DirectManagerRule()), Duration.ofHours(2), LocalDateTime.now());

        assertThrows(IllegalStateException.class, () -> doc.setRoute(List.of()));
    }
}