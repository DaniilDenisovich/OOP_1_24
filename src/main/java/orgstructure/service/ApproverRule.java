package orgstructure.service;

import orgstructure.enums.Role;
import orgstructure.exception.ApprovalException;
import orgstructure.model.Department;
import orgstructure.model.Employee;

import java.util.Objects;


@FunctionalInterface
public interface ApproverRule {


    Employee resolveApprover(Employee author, Department organization) throws ApprovalException;

    static ApproverRule directManager() {
        return (author, organization) -> {
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
        };
    }


    static ApproverRule departmentHead(int levelsUp) {
        if (levelsUp < 0) {
            throw new IllegalArgumentException("Уровень вверх не может быть отрицательным");
        }
        return (author, organization) -> {
            Objects.requireNonNull(author, "Автор не может быть null");
            Department dept = author.getDepartment();
            if (dept == null) {
                throw new ApprovalException("Сотрудник " + author.getFullName() + " не привязан к подразделению");
            }
            return dept.findManagerLevelsUp(levelsUp)
                    .orElseThrow(() -> new ApprovalException("Не удалось найти руководителя на "
                            + levelsUp + " уровней вверх для отдела " + dept.getName()));
        };
    }

    static ApproverRule roleHolder(Role targetRole) {
        Objects.requireNonNull(targetRole, "Целевая роль не может быть null");
        return (author, organization) -> {
            Objects.requireNonNull(organization, "Организация не может быть null");
            return organization.getAllSubordinatesDeep().stream()
                    .filter(emp -> emp.getRole() == targetRole)
                    .findFirst()
                    .orElseThrow(() -> new ApprovalException("В организации не найден сотрудник с ролью: " + targetRole));
        };
    }
}