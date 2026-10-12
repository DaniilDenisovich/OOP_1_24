package orgstructure.service;

import orgstructure.enums.Role;
import orgstructure.exception.ApprovalException;
import orgstructure.model.Department;
import orgstructure.model.Document;
import orgstructure.model.Employee;


public class RoleHolderResolver implements ApproverResolver {
    @Override
    public Employee resolve(Document document, Department currentDept, Role requiredRole) {
        if (requiredRole == null) {
            throw new ApprovalException("Для правила ROLE_HOLDER должна быть указана требуемая роль");
        }

        // Сначала проверяем руководителя подразделения
        if (currentDept.getHead() != null && currentDept.getHead().getRole() == requiredRole) {
            return currentDept.getHead();
        }

        // Затем ищем среди обычных сотрудников подразделения
        for (Employee emp : currentDept.getEmployees()) {
            if (emp.getRole() == requiredRole) {
                return emp;
            }
        }

        throw new ApprovalException("В подразделении '" + currentDept.getName() +
                "' не найден сотрудник с ролью: " + requiredRole.getDisplayName());
    }
}