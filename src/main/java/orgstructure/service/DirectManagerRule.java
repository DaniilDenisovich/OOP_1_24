package orgstructure.service;

import orgstructure.exception.ApprovalException;
import orgstructure.model.Department;
import orgstructure.model.Employee;

import java.util.Objects;


public class DirectManagerRule implements ApproverRule {

    @Override
    public Employee resolveApprover(Employee author, Department organization) throws ApprovalException {
        Objects.requireNonNull(author, "Автор не может быть null");
        Department dept = author.getDepartment();

        if (dept == null) {
            throw new ApprovalException("Сотрудник " + author.getFullName() + " не привязан к подразделению");
        }

        if (author.equals(dept.getHead())) {
            return dept.findManagerLevelsUp(1)
                    .orElseThrow(() -> new ApprovalException("Для руководителя отдела " + dept.getName()
                            + " (" + author.getFullName() + ") нет вышестоящего руководителя"));
        }

        if (dept.getHead() == null) {
            throw new ApprovalException("В подразделении " + dept.getName() + " не назначен руководитель");
        }

        return dept.getHead();
    }

    @Override
    public String getDescription() {
        return "Непосредственный руководитель";
    }
}