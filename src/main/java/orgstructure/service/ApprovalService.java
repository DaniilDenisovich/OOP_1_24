package orgstructure.service;

import orgstructure.enums.ApproverRule;
import orgstructure.enums.Role;
import orgstructure.exception.ApprovalException;
import orgstructure.model.ApprovalStep;
import orgstructure.model.Department;
import orgstructure.model.Document;
import orgstructure.model.Employee;
import java.util.ArrayList;
import java.util.List;

/**
  Сервис для работы с оргструктурой и согласованием документов.
  Реализует обход дерева, поиск руководителей и подчинённых,
  построение маршрута согласования и логику согласования/отклонения.
 */
public class ApprovalService {
    private final List<ApproverResolver> resolvers;

    /**
      Создаёт сервис с набором резолверов для разных правил выбора согласующего.
     */
    public ApprovalService() {
        this.resolvers = new ArrayList<>();
        this.resolvers.add(new DirectManagerResolver());
        this.resolvers.add(new DepartmentHeadResolver());
        this.resolvers.add(new RoleHolderResolver());
    }

    /**
      Обходит дерево подразделений в глубину и собирает все подразделения.
      @param root корневое подразделение
      @return список всех подразделений в дереве
     */
    public List<Department> getAllDepartments(Department root) {
        return root.getAllDepartments();
    }

    /**
      Находит руководителя на N уровней вверх от подразделения сотрудника.
      @param employee сотрудник, от которого поднимаемся
      @param levelsUp количество уровней для подъёма (1 = непосредственный руководитель)
      @return руководитель или null, если достигли корня
      @throws IllegalArgumentException если employee не назначен в подразделение
     */
    public Employee findManagerNLevelsUp(Employee employee, int levelsUp) {
        if (employee.getDepartment() == null) {
            throw new IllegalArgumentException("Сотрудник '" + employee.getFullName() + "' не назначен в подразделение");
        }
        return employee.getDepartment().findManagerNLevelsUp(levelsUp);
    }

    /**
      Собирает всех подчинённых вглубь дерева от указанного подразделения.
      @param department подразделение, от которого ищем подчинённых
      @return список всех сотрудников во всех дочерних подразделениях
     */
    public List<Employee> getAllSubordinates(Department department) {
        return department.getAllSubordinates();
    }

    /**
      Строит маршрут согласования для документа по заданным правилам.
      @param document документ, для которого строится маршрут
      @param department подразделение, в контексте которого строится маршрут
      @param rules список правил выбора согласующего
      @throws IllegalStateException если документ не в статусе DRAFT
     */
    public void buildRoute(Document document, Department department, List<ApproverRule> rules) {
        if (document.getStatus() != orgstructure.enums.DocumentStatus.DRAFT) {
            throw new IllegalStateException("Нельзя построить маршрут для документа в статусе " + document.getStatus());
        }

        for (ApproverRule rule : rules) {
            ApprovalStep step = new ApprovalStep(rule, rule == ApproverRule.ROLE_HOLDER ? Role.LAWYER : null);
            Employee expectedApprover = resolveApprover(step, document, department);
            step.setExpectedApprover(expectedApprover);
            document.addStep(step);
        }
    }

    /**
      Согласует шаг маршрута указанным сотрудником.
      @param document документ
      @param step шаг для согласования
      @param employee сотрудник, который согласует
      @throws ApprovalException если сотрудник не является ожидаемым согласующим
      @throws IllegalStateException если документ не в статусе IN_APPROVAL или шаг уже завершён
     */
    public void approveStep(Document document, ApprovalStep step, Employee employee) {
        validateStepApproval(document, step, employee);
        step.approve(employee);
        document.updateStatus();
    }

    /**
      Отклоняет шаг маршрута указанным сотрудником.
      @param document документ
      @param step шаг для отклонения
      @param employee сотрудник, который отклоняет
      @throws ApprovalException если сотрудник не является ожидаемым согласующим
      @throws IllegalStateException если документ не в статусе IN_APPROVAL или шаг уже завершён
     */
    public void rejectStep(Document document, ApprovalStep step, Employee employee) {
        validateStepApproval(document, step, employee);
        step.reject(employee);
        document.updateStatus();
    }

    /**
      Проверяет, имеет ли сотрудник право согласовать или отклонить указанный шаг.
      @param document документ, проходящий согласование
      @param step шаг маршрута, который пытаются согласовать или отклонить
      @param employee сотрудник, выполняющий действие
      @throws IllegalStateException если документ не в статусе IN_APPROVAL или шаг уже завершён
      @throws ApprovalException если у шага нет ожидающего согласующего или сотрудник не тот
     */

    private void validateStepApproval(Document document, ApprovalStep step, Employee employee) {
        if (document.getStatus() != orgstructure.enums.DocumentStatus.IN_APPROVAL) {
            throw new IllegalStateException("Документ должен быть в статусе IN_APPROVAL для согласования/отклонения");
        }
        if (step.isCompleted()) {
            throw new IllegalStateException("Шаг уже завершён со статусом " + step.getStatus());
        }
        if (step.getExpectedApprover() == null) {
            throw new ApprovalException("Для шага не установлен ожидаемый согласующий");
        }
        if (!step.getExpectedApprover().equals(employee)) {
            throw new ApprovalException("Сотрудник '" + employee.getFullName() +
                    "' не является ожидаемым согласующим для этого шага. Ожидался: " +
                    step.getExpectedApprover().getFullName());
        }
    }

    private Employee resolveApprover(ApprovalStep step, Document document, Department department) {
        for (ApproverResolver resolver : resolvers) {
            if (matchesRule(resolver, step.getRule())) {
                return resolver.resolve(document, department, step.getRequiredRole());
            }
        }
        throw new ApprovalException("Не найден резолвер для правила: " + step.getRule());
    }

    private boolean matchesRule(ApproverResolver resolver, ApproverRule rule) {
        if (rule == ApproverRule.DIRECT_MANAGER && resolver instanceof DirectManagerResolver) return true;
        if (rule == ApproverRule.DEPARTMENT_HEAD && resolver instanceof DepartmentHeadResolver) return true;
        if (rule == ApproverRule.ROLE_HOLDER && resolver instanceof RoleHolderResolver) return true;
        return false;
    }
}