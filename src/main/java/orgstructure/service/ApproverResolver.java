package orgstructure.service;

import orgstructure.enums.Role;
import orgstructure.model.Department;
import orgstructure.model.Document;
import orgstructure.model.Employee;

/**
 * Интерфейс для определения согласующего на шаге маршрута.
 */

public interface ApproverResolver {
    /**
     * Находит сотрудника, который должен согласовать документ на текущем шаге.
     * @param document     документ, проходящий согласование
     * @param currentDept  подразделение, где происходит согласование
     * @param requiredRole требуемая роль
     * @return сотрудник, который должен согласовать документ
     */
    Employee resolve(Document document, Department currentDept, Role requiredRole);
}