package orgstructure.service;

import orgstructure.enums.Role;
import orgstructure.exception.ApprovalException;
import orgstructure.model.Department;
import orgstructure.model.Document;
import orgstructure.model.Employee;

/**
  Согласует руководитель текущего подразделения.
  Реализация интерфейса ApproverResolver для правила DEPARTMENT_HEAD.
 */

public class DepartmentHeadResolver implements ApproverResolver {
    @Override
    public Employee resolve(Document document, Department currentDept, Role requiredRole) {
        Employee head = currentDept.getHead();
        if (head == null) {
            throw new ApprovalException("Не назначен руководитель подразделения '" + currentDept.getName() + "'");
        }
        return head;
    }
}