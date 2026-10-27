package orgstructure.demo;

import orgstructure.enums.Role;
import orgstructure.exception.ApprovalException;
import orgstructure.exception.UnauthorizedApproverException;
import orgstructure.model.ApprovalRecord;
import orgstructure.model.Department;
import orgstructure.model.Document;
import orgstructure.model.Employee;
import orgstructure.service.ApprovalService;
import orgstructure.service.ApproverRule;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Консольная демонстрация всех базовых операций оргструктуры и согласования документов.
 */
public class Demo {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("   ДЕМОНСТРАЦИЯ: ОРГСТРУКТУРА И СОГЛАСОВАНИЕ ДОКУМЕНТОВ (ВАРИАНТ 24)");
        System.out.println("==========================================================================\n");

        List<Object> allCreatedObjects = new ArrayList<>();

        // 1. Создание оргструктуры
        System.out.println(">>> 1. Инициализация оргструктуры предприятия...");
        Department rootCompany = new Department("DEP-01", "ПАО 'ТехноХолдинг'");
        Employee ceo = new Employee("EMP-001", "Виктор Семенович Орлов", Role.DIRECTOR);
        rootCompany.setHead(ceo);

        Employee legalHead = new Employee("EMP-002", "Анна Сергеевна Белова", Role.LAWYER);
        Employee accountant = new Employee("EMP-003", "Ирина Петровна Соколова", Role.ACCOUNTANT);
        Employee hrLead = new Employee("EMP-004", "Елена Васильевна Жукова", Role.HR);
        rootCompany.addEmployee(legalHead);
        rootCompany.addEmployee(accountant);
        rootCompany.addEmployee(hrLead);

        Department itDept = new Department("DEP-02", "Департамент информационных технологий");
        Employee cto = new Employee("EMP-005", "Борис Игоревич Морозов", Role.DEPARTMENT_HEAD);
        itDept.setHead(cto);
        rootCompany.addSubDepartment(itDept);

        Department devDept = new Department("DEP-03", "Отдел программной разработки");
        Employee devLead = new Employee("EMP-006", "Сергей Алексеевич Петров", Role.TEAM_LEAD);
        devDept.setHead(devLead);
        itDept.addSubDepartment(devDept);

        Employee dev1 = new Employee("EMP-007", "Алексей Смирнов", Role.DEVELOPER);
        Employee dev2 = new Employee("EMP-008", "Дмитрий Козлов", Role.DEVELOPER);
        Employee dev3 = new Employee("EMP-009", "Мария Новикова", Role.DEVELOPER);
        devDept.addEmployee(dev1);
        devDept.addEmployee(dev2);
        devDept.addEmployee(dev3);

        Department qaDept = new Department("DEP-04", "Отдел обеспечения качества");
        Employee qaLead = new Employee("EMP-010", "Ольга Николаевна Федорова", Role.TEAM_LEAD);
        qaDept.setHead(qaLead);
        itDept.addSubDepartment(qaDept);

        Employee qa1 = new Employee("EMP-011", "Павел Васильев", Role.EMPLOYEE);
        Employee qa2 = new Employee("EMP-012", "Светлана Кузнецова", Role.EMPLOYEE);
        qaDept.addEmployee(qa1);
        qaDept.addEmployee(qa2);

        Department salesDept = new Department("DEP-05", "Департамент корпоративных продаж");
        Employee salesDirector = new Employee("EMP-013", "Григорий Романович Волков", Role.DEPARTMENT_HEAD);
        salesDept.setHead(salesDirector);
        rootCompany.addSubDepartment(salesDept);

        Employee sales1 = new Employee("EMP-014", "Андрей Михайлов", Role.EMPLOYEE);
        Employee sales2 = new Employee("EMP-015", "Татьяна Попова", Role.EMPLOYEE);
        salesDept.addEmployee(sales1);
        salesDept.addEmployee(sales2);

        allCreatedObjects.addAll(List.of(rootCompany, itDept, devDept, qaDept, salesDept));
        allCreatedObjects.addAll(List.of(ceo, legalHead, accountant, hrLead, cto, devLead,
                dev1, dev2, dev3, qaLead, qa1, qa2, salesDirector, sales1, sales2));

        System.out.println("Оргструктура успешно построена: 5 подразделений, 15 сотрудников.");

        // 2. Обходы дерева
        System.out.println("\n>>> 2. Демонстрация обходов дерева подразделений:");
        System.out.println("Руководитель на 1 уровень выше: "
                + devDept.findManagerLevelsUp(1).map(Employee::getFullName).orElse("отсутствует"));
        System.out.println("Руководитель на 2 уровня выше: "
                + devDept.findManagerLevelsUp(2).map(Employee::getFullName).orElse("отсутствует"));

        List<Employee> allStaff = rootCompany.getAllSubordinatesDeep();
        System.out.println("Всего найдено сотрудников в холдинге: " + allStaff.size());

        // 3. Согласование документа
        System.out.println("\n>>> 3. Сценарий: успешное согласование служебной записки");
        ApprovalService service = new ApprovalService(rootCompany);

        LocalDateTime startTime = LocalDateTime.of(2026, 10, 25, 10, 0);
        Document doc1 = new Document("DOC-2026-001", "Заявка на сервер", dev1, startTime);
        allCreatedObjects.add(doc1);

        List<ApproverRule> rules = List.of(
                ApproverRule.directManager(),
                ApproverRule.roleHolder(Role.LAWYER),
                ApproverRule.departmentHead(2)
        );
        allCreatedObjects.addAll(rules);

        try {
            service.buildRoute(doc1, rules, Duration.ofHours(24), startTime);
            allCreatedObjects.addAll(doc1.getRoute());

            System.out.println("Документ создан: " + doc1);
            doc1.getRoute().forEach(step -> System.out.println("  " + step));

            LocalDateTime t1 = startTime.plusHours(2);
            service.approve(doc1, devLead, "Согласовано тимлидом", t1);

            LocalDateTime t2 = t1.plusHours(4);
            service.approve(doc1, legalHead, "Условия договора согласованы", t2);

            LocalDateTime t3 = t2.plusHours(5);
            service.approve(doc1, ceo, "Утверждаю", t3);

            allCreatedObjects.addAll(doc1.getHistory());

            System.out.println("Итоговое состояние документа: " + doc1);
            for (ApprovalRecord rec : doc1.getHistory()) {
                System.out.printf("  [%s] %s | %s | %s%n",
                        rec.timestamp().format(FORMATTER),
                        rec.approver().getFullName(),
                        rec.status().getDescription(),
                        rec.comment());
            }
        } catch (ApprovalException e) {
            System.err.println("Ошибка маршрутизации: " + e.getMessage());
        }

        // 4. Отклонение
        System.out.println("\n>>> 4. Сценарий: отклонение заявки");
        Document doc2 = new Document("DOC-2026-002", "Командировка", dev2, startTime);
        allCreatedObjects.add(doc2);

        try {
            service.buildRoute(doc2, List.of(ApproverRule.directManager()), Duration.ofDays(2), startTime);
            allCreatedObjects.addAll(doc2.getRoute());

            service.reject(doc2, devLead, "Релиз проекта, отклонено", startTime.plusHours(1));
            allCreatedObjects.addAll(doc2.getHistory());

            System.out.println("Результат: статус = " + doc2.getStatus().getTitle());
        } catch (ApprovalException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }

        // 5. Проверка исключений
        System.out.println("\n>>> 5. Демонстрация исключений обоих видов:");

        // Unchecked
        Document doc3 = new Document("DOC-2026-003", "Премия", qa1, startTime);
        allCreatedObjects.add(doc3);
        try {
            service.buildRoute(doc3, List.of(ApproverRule.directManager()), Duration.ofHours(12), startTime);
            allCreatedObjects.addAll(doc3.getRoute());
            service.approve(doc3, dev1, "Одобряю коллегу!", startTime.plusHours(1));
        } catch (UnauthorizedApproverException ex) {
            System.out.println(" [ПЕРЕХВАЧЕНО НЕПРОВЕРЯЕМОЕ ИСКЛЮЧЕНИЕ]: " + ex.getMessage());
        } catch (ApprovalException e) {
            System.err.println("Неожиданная ошибка: " + e.getMessage());
        }

        // Checked
        Document doc4 = new Document("DOC-2026-004", "Устав", ceo, startTime);
        allCreatedObjects.add(doc4);
        try {
            service.buildRoute(doc4, List.of(ApproverRule.directManager()), Duration.ofHours(8), startTime);
        } catch (ApprovalException ex) {
            System.out.println(" [ПЕРЕХВАЧЕНО ПРОВЕРЯЕМОЕ ИСКЛЮЧЕНИЕ]: " + ex.getMessage());
        }

        System.out.println("\n==========================================================================");
        System.out.printf("ИТОГО: задействовано %d объектов предметной области.%n", allCreatedObjects.size());
        System.out.println("==========================================================================");
    }
}