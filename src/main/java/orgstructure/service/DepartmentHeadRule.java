package orgstructure.service;

import orgstructure.exception.ApprovalException;
import orgstructure.model.Department;
import orgstructure.model.Employee;

import java.util.Objects;

public class DepartmentHeadRule implements ApproverRule {

    private final int levelsUp;


    public DepartmentHeadRule(int levelsUp) {
        if (levelsUp < 0) {
            throw new IllegalArgumentException("Уровень вверх не может быть отрицательным");
        }
        this.levelsUp = levelsUp;
    }

    @Override
    public Employee resolveApprover(Employee author, Department organization) throws ApprovalException {
        Objects.requireNonNull(author, "Автор не может быть null");
        Department dept = author.getDepartment();

        if (dept == null) {
            throw new ApprovalException("Сотрудник " + author.getFullName() + " не привязан к подразделению");
        }

        return dept.findManagerLevelsUp(levelsUp)
                .orElseThrow(() -> new ApprovalException("Не удалось найти руководителя на "
                        + levelsUp + " уровней вверх для отдела " + dept.getName()));
    }

    @Override
    public String getDescription() {
        return "Руководитель подразделения (уровней вверх: " + levelsUp + ")";
    }
}