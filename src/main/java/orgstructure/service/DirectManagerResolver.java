package orgstructure.service;

import orgstructure.enums.Role;
import orgstructure.exception.ApprovalException;
import orgstructure.model.Department;
import orgstructure.model.Document;
import orgstructure.model.Employee;


public class DirectManagerResolver implements ApproverResolver {
    @Override
    public Employee resolve(Document document, Department currentDept, Role requiredRole) {
        // В реальной системе мы бы знали автора документа.
        // Для упрощения базового уровня считаем, что руководитель текущего подразделения и есть согласующий.
        Employee head = currentDept.getHead();
        if (head == null) {
            throw new ApprovalException("Не назначен руководитель подразделения '" + currentDept.getName() + "'");
        }
        return head;
    }
}