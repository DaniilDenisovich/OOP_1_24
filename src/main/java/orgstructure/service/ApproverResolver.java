package orgstructure.service;

import orgstructure.enums.Role;
import orgstructure.model.Department;
import orgstructure.model.Document;
import orgstructure.model.Employee;



public interface ApproverResolver {

    Employee resolve(Document document, Department currentDept, Role requiredRole);
}