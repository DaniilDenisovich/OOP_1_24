package orgstructure.service;

import orgstructure.enums.Role;
import orgstructure.exception.ApprovalException;
import orgstructure.model.Department;
import orgstructure.model.Document;
import orgstructure.model.Employee;

/**
  Согласует непосредственный руководитель сотрудника.
  Реализация интерфейса ApproverResolver для правила DIRECT_MANAGER.
  В возвращает руководителя текущего подразделения.
 */
public class DirectManagerResolver implements ApproverResolver {
    @Override

    /**
      Находит непосредственного руководителя сотрудника в указанном подразделении.
      @param document документ, проходящий согласование
      @param currentDept подразделение, в контексте которого происходит поиск
      @param requiredRole требуемая роль
      @return руководитель подразделения
      @throws ApprovalException если в подразделении не назначен руководитель
     */
    public Employee resolve(Document document, Department currentDept, Role requiredRole) {

        Employee head = currentDept.getHead();
        if (head == null) {
            throw new ApprovalException("Не назначен руководитель подразделения '" + currentDept.getName() + "'");
        }
        return head;
    }
}