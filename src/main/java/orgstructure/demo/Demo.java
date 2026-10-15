package orgstructure.demo;

import orgstructure.enums.ApproverRule;
import orgstructure.enums.DocumentStatus;
import orgstructure.enums.Role;
import orgstructure.enums.StepStatus;
import orgstructure.exception.ApprovalException;
import orgstructure.model.ApprovalStep;
import orgstructure.model.Department;
import orgstructure.model.Document;
import orgstructure.model.Employee;
import orgstructure.service.ApprovalService;

import java.util.List;


public class Demo {

    public static void main(String[] args) {
        System.out.println("=== Демонстрация: Оргструктура и согласование документов ===\n");

        System.out.println("--- 1. Создание оргструктуры ---");
        Department company = createOrganization();
        printDepartmentTree(company, 0);
        System.out.println();

        System.out.println("--- 2. Обход дерева подразделений ---");
        ApprovalService service = new ApprovalService();
        List<Department> allDepts = service.getAllDepartments(company);
        System.out.println("Всего подразделений в компании: " + allDepts.size());
        for (Department dept : allDepts) {
            System.out.println("  - " + dept.getName());
        }
        System.out.println();

        System.out.println("--- 3. Поиск руководителя на N уровней вверх ---");
        Employee engineer = findEmployee(company, "Инженер 1");
        if (engineer != null) {
            System.out.println("Сотрудник: " + engineer.getFullName() + " из " + engineer.getDepartment().getName());
            Employee manager1 = service.findManagerNLevelsUp(engineer, 1);
            System.out.println("  Руководитель на 1 уровень вверх: " + (manager1 != null ? manager1.getFullName() : "нет"));
            Employee manager2 = service.findManagerNLevelsUp(engineer, 2);
            System.out.println("  Руководитель на 2 уровня вверх: " + (manager2 != null ? manager2.getFullName() : "нет"));
            Employee manager3 = service.findManagerNLevelsUp(engineer, 3);
            System.out.println("  Руководитель на 3 уровня вверх: " + (manager3 != null ? manager3.getFullName() : "нет"));
        }
        System.out.println();

        System.out.println("--- 4. Поиск всех подчинённых вглубь ---");
        Department itDept = findDepartment(company, "IT отдел");
        if (itDept != null) {
            List<Employee> subordinates = service.getAllSubordinates(itDept);
            System.out.println("Подчинённые IT отдела (включая вложенные): " + subordinates.size());
            for (Employee emp : subordinates) {
                System.out.println("  - " + emp.getFullName() + " (" + emp.getRole().getDisplayName() + ")");
            }
        }
        System.out.println();

        System.out.println("--- 5. Построение маршрута согласования ---");
        Document doc = new Document("Договор на поставку оборудования");
        System.out.println("Создан документ: " + doc.getTitle() + " [статус: " + doc.getStatus().getDisplayName() + "]");

        List<ApproverRule> rules = List.of(
                ApproverRule.DIRECT_MANAGER,
                ApproverRule.DEPARTMENT_HEAD,
                ApproverRule.ROLE_HOLDER
        );
        service.buildRoute(doc, itDept, rules);
        System.out.println("Маршрут согласования построен:");
        for (ApprovalStep step : doc.getSteps()) {
            System.out.println("  - " + step);
        }
        System.out.println();

        System.out.println("--- 6. Запуск согласования ---");
        doc.startApproval();
        System.out.println("Документ переведён в статус: " + doc.getStatus().getDisplayName());
        System.out.println();

        System.out.println("--- 7. Согласование шагов ---");
        for (ApprovalStep step : doc.getSteps()) {
            Employee expectedApprover = step.getExpectedApprover();
            System.out.println("Шаг: " + step.getRule().getDisplayName());
            System.out.println("  Ожидаемый согласующий: " + (expectedApprover != null ? expectedApprover.getFullName() : "не назначен"));

            if (expectedApprover != null) {
                service.approveStep(doc, step, expectedApprover);
                System.out.println("  Статус после согласования: " + step.getStatus().getDisplayName());
                System.out.println("  Статус документа: " + doc.getStatus().getDisplayName());
            }
            System.out.println();
        }

        System.out.println("--- 8. История документа ---");
        System.out.println("История изменений:");
        for (String entry : doc.getHistory()) {
            System.out.println("  - " + entry);
        }
        System.out.println();

        System.out.println("--- 9. Попытка согласовать чужой шаг (демонстрация исключения) ---");
        Document doc2 = new Document("Служебная записка");
        service.buildRoute(doc2, itDept, List.of(ApproverRule.DIRECT_MANAGER));
        doc2.startApproval();
        ApprovalStep step2 = doc2.getCurrentStep();

        Employee wrongEmployee = findEmployee(company, "Инженер 1");
        System.out.println("Попытка согласовать шаг сотрудником: " + wrongEmployee.getFullName());
        System.out.println("Ожидаемый согласующий: " + (step2.getExpectedApprover() != null ? step2.getExpectedApprover().getFullName() : "не назначен"));

        try {
            service.approveStep(doc2, step2, wrongEmployee);
            System.out.println("  Ошибка: исключение не выброшено!");
        } catch (ApprovalException e) {
            System.out.println("  Исключение поймано: " + e.getMessage());
        }
        System.out.println();

        System.out.println("--- 10. Отклонение шага ---");
        Document doc3 = new Document("Заявка на отпуск");
        service.buildRoute(doc3, itDept, List.of(ApproverRule.DIRECT_MANAGER, ApproverRule.DEPARTMENT_HEAD));
        doc3.startApproval();
        ApprovalStep step3 = doc3.getCurrentStep();

        if (step3.getExpectedApprover() != null) {
            service.rejectStep(doc3, step3, step3.getExpectedApprover());
            System.out.println("Шаг отклонён. Статус документа: " + doc3.getStatus().getDisplayName());
        }
        System.out.println();

        System.out.println("=== Демонстрация завершена ===");
    }

    private static Department createOrganization() {
        Department company = new Department("ООО Ромашка");
        Employee director = new Employee("Иванов И.И.", Role.DIRECTOR);
        company.setHead(director);
        company.addEmployee(director);
        director.setDepartment(company);

        Department accounting = new Department("Бухгалтерия");
        Employee accHead = new Employee("Сидорова С.С.", Role.DEPARTMENT_HEAD);
        accounting.setHead(accHead);
        accounting.addEmployee(accHead);
        accHead.setDepartment(accounting);
        company.addChild(accounting);

        for (int i = 1; i <= 4; i++) {
            Employee acc = new Employee("Бухгалтер " + i, Role.ACCOUNTANT);
            accounting.addEmployee(acc);
            acc.setDepartment(accounting);
        }

        Department it = new Department("IT отдел");
        Employee itHead = new Employee("Кузнецов К.К.", Role.DEPARTMENT_HEAD);
        it.setHead(itHead);
        it.addEmployee(itHead);
        itHead.setDepartment(it);
        company.addChild(it);

        for (int i = 1; i <= 5; i++) {
            Employee eng = new Employee("Инженер " + i, Role.ENGINEER);
            it.addEmployee(eng);
            eng.setDepartment(it);
        }

        Employee lawyer = new Employee("Юрист Л.Л.", Role.LAWYER);
        it.addEmployee(lawyer);
        lawyer.setDepartment(it);

        Department dev = new Department("Отдел разработки");
        Employee devHead = new Employee("Петров П.П.", Role.DEPARTMENT_HEAD);
        dev.setHead(devHead);
        dev.addEmployee(devHead);
        devHead.setDepartment(dev);
        it.addChild(dev);

        for (int i = 1; i <= 6; i++) {
            Employee devEng = new Employee("Разработчик " + i, Role.ENGINEER);
            dev.addEmployee(devEng);
            devEng.setDepartment(dev);
        }

        Department qa = new Department("Отдел тестирования");
        Employee qaHead = new Employee("Тестировщиков Т.Т.", Role.DEPARTMENT_HEAD);
        qa.setHead(qaHead);
        qa.addEmployee(qaHead);
        qaHead.setDepartment(qa);
        it.addChild(qa);

        for (int i = 1; i <= 4; i++) {
            Employee tester = new Employee("Тестировщик " + i, Role.ENGINEER);
            qa.addEmployee(tester);
            tester.setDepartment(qa);
        }

        Department sales = new Department("Отдел продаж");
        Employee salesHead = new Employee("Продавцов П.П.", Role.DEPARTMENT_HEAD);
        sales.setHead(salesHead);
        sales.addEmployee(salesHead);
        salesHead.setDepartment(sales);
        company.addChild(sales);

        for (int i = 1; i <= 5; i++) {
            Employee manager = new Employee("Менеджер " + i, Role.MANAGER);
            sales.addEmployee(manager);
            manager.setDepartment(sales);
        }

        return company;
    }


    private static void printDepartmentTree(Department dept, int level) {
        String indent = "  ".repeat(level);
        System.out.println(indent + dept.getName());
        for (Department child : dept.getChildren()) {
            printDepartmentTree(child, level + 1);
        }
    }


    private static Employee findEmployee(Department root, String fullName) {
        for (Department dept : root.getAllDepartments()) {
            if (dept.getHead() != null && dept.getHead().getFullName().equals(fullName)) {
                return dept.getHead();
            }
            for (Employee emp : dept.getEmployees()) {
                if (emp.getFullName().equals(fullName)) {
                    return emp;
                }
            }
        }
        return null;
    }


    private static Department findDepartment(Department root, String name) {
        for (Department dept : root.getAllDepartments()) {
            if (dept.getName().equals(name)) {
                return dept;
            }
        }
        return null;
    }
}