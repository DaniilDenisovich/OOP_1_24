package orgstructure.service;

import orgstructure.enums.ApproverRule;
import orgstructure.enums.Role;
import orgstructure.exception.ApprovalException;
import orgstructure.model.ApprovalStep;
import orgstructure.model.Department;
import orgstructure.model.Document;
import orgstructure.model.Employee;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ApprovalService {
    private final List<ApproverResolver> resolvers;

    public ApprovalService() {
        this.resolvers = new ArrayList<>();
        this.resolvers.add(new DirectManagerResolver());
        this.resolvers.add(new DepartmentHeadResolver());
        this.resolvers.add(new RoleHolderResolver());
    }

    public List<Department> getAllDepartments(Department root) {
        return root.getAllDepartments();
    }


    public Employee findManagerNLevelsUp(Employee employee, int levelsUp) {
        if (employee.getDepartment() == null) {
            throw new IllegalArgumentException("Сотрудник '" + employee.getFullName() + "' не назначен в подразделение");
        }
        return employee.getDepartment().findManagerNLevelsUp(levelsUp);
    }


    public List<Employee> getAllSubordinates(Department department) {
        return department.getAllSubordinates();
    }


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


    public void approveStep(Document document, ApprovalStep step, Employee employee) {
        validateStepApproval(document, step, employee);
        step.approve(employee);
        document.updateStatus();
    }


    public void rejectStep(Document document, ApprovalStep step, Employee employee) {
        validateStepApproval(document, step, employee);
        step.reject(employee);
        document.updateStatus();
    }


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
            // Находим подходящий резолвер по правилу
            if (matchesRule(resolver, step.getRule())) {
                return resolver.resolve(document, department, step.getRequiredRole());
            }
        }
        throw new ApprovalException("Не найден резолвер для правила: " + step.getRule());
    }


    private boolean matchesRule(ApproverResolver resolver, ApproverRule rule) {
        if (rule == ApproverRule.DIRECT_MANAGER && resolver instanceof DirectManagerResolver) {
            return true;
        }
        if (rule == ApproverRule.DEPARTMENT_HEAD && resolver instanceof DepartmentHeadResolver) {
            return true;
        }
        if (rule == ApproverRule.ROLE_HOLDER && resolver instanceof RoleHolderResolver) {
            return true;
        }
        return false;
    }
}