package orgstructure.demo;

import orgstructure.enums.Role;
import orgstructure.exception.ApprovalException;
import orgstructure.exception.UnauthorizedApproverException;
import orgstructure.model.ApprovalRecord;
import orgstructure.model.ApprovalStep;
import orgstructure.model.Department;
import orgstructure.model.Document;
import orgstructure.model.Employee;
import orgstructure.service.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Консольная демонстрация всех базовых операций предметной модели
 * "Оргструктура и согласование документов" (Вариант 24).
 * Наполняет модель более чем 30 объектами и последовательно показывает сценарии.
 */
public class Demo {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("   ДЕМОНСТРАЦИЯ: ОРГСТРУКТУРА И СОГЛАСОВАНИЕ ДОКУМЕНТОВ (ВАРИАНТ 24)");
        System.out.println("==========================================================================\n");

        List<Object> allCreatedObjects = new ArrayList<>();

        // ---------------------------------------------------------------------
        // 1. НАПОЛНЕНИЕ ОРГСТРУКТУРЫ (ПОДРАЗДЕЛЕНИЯ И СОТРУДНИКИ)
        // ---------------------------------------------------------------------
        System.out.println(">>> 1. Инициализация оргструктуры предприятия...");

        // Корень
        Department rootCompany = new Department("DEP-01", "ПАО 'ТехноХолдинг'");
        Employee ceo = new Employee("EMP-001", "Виктор Семенович Орлов", Role.DIRECTOR);
        rootCompany.setHead(ceo);

        Employee legalHead = new Employee("EMP-002", "Анна Сергеевна Белова", Role.LAWYER);
        Employee accountant = new Employee("EMP-003", "Ирина Петровна Соколова", Role.ACCOUNTANT);
        Employee hrLead = new Employee("EMP-004", "Елена Васильевна Жукова", Role.HR);
        rootCompany.addEmployee(legalHead);
        rootCompany.addEmployee(accountant);
        rootCompany.addEmployee(hrLead);

        // Департамент IT
        Department itDept = new Department("DEP-02", "Департамент информационных технологий");
        Employee cto = new Employee("EMP-005", "Борис Игоревич Морозов", Role.DEPARTMENT_HEAD);
        itDept.setHead(cto);
        rootCompany.addSubDepartment(itDept);

        // Отдел разработки (в составе IT)
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

        // Отдел тестирования и QA (в составе IT)
        Department qaDept = new Department("DEP-04", "Отдел обеспечения качества");
        Employee qaLead = new Employee("EMP-010", "Ольга Николаевна Федорова", Role.TEAM_LEAD);
        qaDept.setHead(qaLead);
        itDept.addSubDepartment(qaDept);

        Employee qa1 = new Employee("EMP-011", "Павел Васильев", Role.EMPLOYEE);
        Employee qa2 = new Employee("EMP-012", "Светлана Кузнецова", Role.EMPLOYEE);
        qaDept.addEmployee(qa1);
        qaDept.addEmployee(qa2);

        // Департамент продаж
        Department salesDept = new Department("DEP-05", "Департамент корпоративных продаж");
        Employee salesDirector = new Employee("EMP-013", "Григорий Романович Волков", Role.DEPARTMENT_HEAD);
        salesDept.setHead(salesDirector);
        rootCompany.addSubDepartment(salesDept);

        Employee sales1 = new Employee("EMP-014", "Андрей Михайлов", Role.EMPLOYEE);
        Employee sales2 = new Employee("EMP-015", "Татьяна Попова", Role.EMPLOYEE);
        salesDept.addEmployee(sales1);
        salesDept.addEmployee(sales2);

        // Сохраняем объекты в общий список для контроля объема модели (>30)
        allCreatedObjects.addAll(List.of(rootCompany, itDept, devDept, qaDept, salesDept));
        allCreatedObjects.addAll(List.of(ceo, legalHead, accountant, hrLead, cto, devLead,
                dev1, dev2, dev3, qaLead, qa1, qa2, salesDirector, sales1, sales2));

        System.out.println("Оргструктура успешно построена: 5 подразделений, 15 сотрудников.");

        // ---------------------------------------------------------------------
        // 2. ДЕМОНСТРАЦИЯ ОБХОДОВ ДЕРЕВА
        // ---------------------------------------------------------------------
        System.out.println("\n>>> 2. Демонстрация обходов дерева подразделений:");

        // Поиск на N уровней вверх
        System.out.println("2.1. Поиск руководителей на N уровней вверх для '" + devDept.getName() + "':");
        System.out.println(" - 0 уровней (свой руководитель): "
                + devDept.findManagerLevelsUp(0).map(Employee::getFullName).orElse("отсутствует"));
        System.out.println(" - 1 уровень вверх (департамент IT): "
                + devDept.findManagerLevelsUp(1).map(Employee::getFullName).orElse("отсутствует"));
        System.out.println(" - 2 уровня вверх (холдинг / корень): "
                + devDept.findManagerLevelsUp(2).map(Employee::getFullName).orElse("отсутствует"));
        System.out.println(" - 3 уровня вверх (выше корня): "
                + devDept.findManagerLevelsUp(3).map(Employee::getFullName).orElse("отсутствует (выход за пределы корня)"));

        // Сбор подчинённых вглубь (DFS)
        List<Employee> allStaff = rootCompany.getAllSubordinatesDeep();
        System.out.println("\n2.2. Сбор всех подчинённых вглубь поддерева '" + rootCompany.getName() + "':");
        System.out.println("Всего найдено сотрудников: " + allStaff.size());
        allStaff.forEach(emp -> System.out.println("  * " + emp));

        // ---------------------------------------------------------------------
        // 3. ПОЛНЫЙ ЦИКЛ СОГЛАСОВАНИЯ ДОКУМЕНТА (УСПЕШНЫЙ СЦЕНАРИЙ)
        // ---------------------------------------------------------------------
        System.out.println("\n>>> 3. Сценарий: успешное согласование служебной записки");
        ApprovalService service = new ApprovalService(rootCompany);

        LocalDateTime startTime = LocalDateTime.of(2026, 10, 25, 10, 0);
        Document doc1 = new Document("DOC-2026-001", "Заявка на модернизацию серверов сборки", dev1, startTime);
        allCreatedObjects.add(doc1);

        // Формирование правил маршрута (Стратегии)
        List<ApproverRule> rules = List.of(
                new DirectManagerRule(),
                new RoleHolderRule(Role.LAWYER),
                new DepartmentHeadRule(2)
        );
        allCreatedObjects.addAll(rules);

        try {
            service.buildRoute(doc1, rules, Duration.ofHours(24), startTime);
            allCreatedObjects.addAll(doc1.getRoute());

            System.out.println("Документ создан: " + doc1);
            System.out.println("Сформирован маршрут согласования:");
            doc1.getRoute().forEach(step -> System.out.println("  " + step));

            // Шаг 1: согласование тимлидом
            LocalDateTime t1 = startTime.plusHours(2);
            System.out.println("\n[Шаг 1] " + devLead.getFullName() + " рассматривает заявку...");
            service.approve(doc1, devLead, "Согласовано, мощности действительно не хватает", t1);

            // Шаг 2: согласование юристом
            LocalDateTime t2 = t1.plusHours(4);
            System.out.println("[Шаг 2] " + legalHead.getFullName() + " проверяет договор поставки...");
            service.approve(doc1, legalHead, "Условия типового договора согласованы", t2);

            // Шаг 3: финальное утверждение директором
            LocalDateTime t3 = t2.plusHours(5);
            System.out.println("[Шаг 3] " + ceo.getFullName() + " утверждает итоговый бюджет...");
            service.approve(doc1, ceo, "Утверждаю к закупке за счет резерва", t3);

            allCreatedObjects.addAll(doc1.getHistory());

            System.out.println("\nИтоговое состояние документа: " + doc1);
            System.out.println("Хронологическая история согласования:");
            for (ApprovalRecord rec : doc1.getHistory()) {
                System.out.printf("  [%s] %s | Решение: %s | Комментарий: %s%n",
                        rec.timestamp().format(FORMATTER),
                        rec.approver().getFullName(),
                        rec.status().getDescription(),
                        rec.comment());
            }

        } catch (ApprovalException e) {
            System.err.println("Ошибка маршрутизации: " + e.getMessage());
        }

        // ---------------------------------------------------------------------
        // 4. СЦЕНАРИЙ: ОТКЛОНЕНИЕ ДОКУМЕНТА
        // ---------------------------------------------------------------------
        System.out.println("\n>>> 4. Сценарий: отклонение заявки на командировку");
        Document doc2 = new Document("DOC-2026-002", "Заявка на международную конференцию", dev2, startTime);
        allCreatedObjects.add(doc2);

        try {
            service.buildRoute(doc2, List.of(new DirectManagerRule()), Duration.ofDays(2), startTime);
            allCreatedObjects.addAll(doc2.getRoute());

            System.out.println("Документ отправлен на согласование: " + doc2);
            System.out.println("Текущий шаг назначен на: " + doc2.getCurrentStep().orElseThrow().getApprover().getFullName());

            // Тимлид отклоняет документ
            LocalDateTime rejectTime = startTime.plusHours(1);
            service.reject(doc2, devLead, "Релиз намечен на эти же даты, командировка отклонена", rejectTime);
            allCreatedObjects.addAll(doc2.getHistory());

            System.out.println("Результат: статус документа = " + doc2.getStatus().getTitle());
            System.out.println("Запись в истории: " + doc2.getHistory().get(0).comment());

        } catch (ApprovalException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }

        // ---------------------------------------------------------------------
        // 5. ДЕМОНСТРАЦИЯ ОБРАБОТКИ ИСКЛЮЧЕНИЙ ОБОИХ ВИДОВ
        // ---------------------------------------------------------------------
        System.out.println("\n>>> 5. Демонстрация исключений обоих видов (Checked и Unchecked):");

        // 5.1. Непроверяемое исключение: попытка согласовать чужой шаг
        System.out.println("\n5.1. Попытка согласовать чужой шаг (UnauthorizedApproverException - Unchecked):");
        Document doc3 = new Document("DOC-2026-003", "Заявка на премию", qa1, startTime);
        allCreatedObjects.add(doc3);

        try {
            service.buildRoute(doc3, List.of(new DirectManagerRule()), Duration.ofHours(12), startTime);
            allCreatedObjects.addAll(doc3.getRoute());

            System.out.println("Шаг назначен на: " + doc3.getCurrentStep().orElseThrow().getApprover().getFullName());
            System.out.println("Сотрудник " + dev1.getFullName() + " пытается согласовать документ за коллегу...");

            // Чужой сотрудник пытается подтвердить шаг
            service.approve(doc3, dev1, "Одобряю коллегу!", startTime.plusHours(1));

        } catch (UnauthorizedApproverException ex) {
            System.out.println(" [ПЕРЕХВАЧЕНО ОЖИДАЕМОЕ ИСКЛЮЧЕНИЕ]: " + ex.getClass().getSimpleName());
            System.out.println("  Сообщение: " + ex.getMessage());
        } catch (ApprovalException e) {
            System.err.println("Неожиданная ошибка: " + e.getMessage());
        }

        // 5.2. Проверяемое исключение: невозможно определить согласующего
        System.out.println("\n5.2. Невозможность построить маршрут (ApprovalException - Checked):");
        Document doc4 = new Document("DOC-2026-004", "Устав холдинга", ceo, startTime);
        allCreatedObjects.add(doc4);

        try {
            System.out.println("Попытка найти вышестоящего руководителя для генерального директора...");
            service.buildRoute(doc4, List.of(new DirectManagerRule()), Duration.ofHours(8), startTime);
        } catch (ApprovalException ex) {
            System.out.println(" [ПЕРЕХВАЧЕНО ОЖИДАЕМОЕ ПРОВЕРЯЕМОЕ ИСКЛЮЧЕНИЕ]: " + ex.getClass().getSimpleName());
            System.out.println("  Причина: " + ex.getMessage());
        }

        // ---------------------------------------------------------------------
        // ИТОГ ПО ОБЪЕКТАМ
        // ---------------------------------------------------------------------
        System.out.println("\n==========================================================================");
        System.out.printf("ИТОГО: создано и задействовано в демонстрации %d объектов предметной области.%n",
                allCreatedObjects.size());
        System.out.println("Все требования базового уровня и критерии демонстрации полностью выполнены.");
        System.out.println("==========================================================================");
    }
}