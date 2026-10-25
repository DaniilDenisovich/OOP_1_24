package orgstructure.service;

import orgstructure.enums.Role;
import orgstructure.exception.ApprovalException;
import orgstructure.model.Department;
import orgstructure.model.Employee;

import java.util.Objects;


public class RoleHolderRule implements ApproverRule {

    private final Role targetRole;

    public RoleHolderRule(Role targetRole) {
        this.targetRole = Objects.requireNonNull(targetRole, "Целевая роль не может быть null");
    }

    @Override
    public Employee resolveApprover(Employee author, Department organization) throws ApprovalException {
        Objects.requireNonNull(organization, "Организация не может быть null");

        return organization.getAllSubordinatesDeep().stream()
                .filter(emp -> emp.getRole() == targetRole)
                .findFirst()
                .orElseThrow(() -> new ApprovalException("В организации не найден сотрудник с ролью: " + targetRole));
    }

    @Override
    public String getDescription() {
        return "Сотрудник с ролью " + targetRole;
    }
}