package orgstructure.service;

import orgstructure.exception.ApprovalException;
import orgstructure.model.Department;
import orgstructure.model.Employee;


public interface ApproverRule {


    Employee resolveApprover(Employee author, Department organization) throws ApprovalException;


    String getDescription();
}