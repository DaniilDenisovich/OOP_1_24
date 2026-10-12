package orgstructure.model;

import orgstructure.enums.ApproverRule;
import orgstructure.enums.Role;
import orgstructure.enums.StepStatus;

import java.time.LocalDate;
import java.util.Objects;


public class ApprovalStep {
    private final ApproverRule rule;
    private final Role requiredRole;
    private StepStatus status;
    private Employee approver;
    private LocalDate approvalDate;
    private final LocalDate createdDate;


    public ApprovalStep(ApproverRule rule, Role requiredRole) {
        if (rule == null) {
            throw new IllegalArgumentException("Правило выбора согласующего не может быть null");
        }
        if (rule == ApproverRule.ROLE_HOLDER && requiredRole == null) {
            throw new IllegalArgumentException("Для правила ROLE_HOLDER должна быть указана требуемая роль");
        }
        this.rule = rule;
        this.requiredRole = requiredRole;
        this.status = StepStatus.PENDING;
        this.createdDate = LocalDate.now();
    }


    public ApproverRule getRule() {
        return rule;
    }


    public Role getRequiredRole() {
        return requiredRole;
    }


    public StepStatus getStatus() {
        return status;
    }

    public Employee getApprover() {
        return approver;
    }

    public LocalDate getApprovalDate() {
        return approvalDate;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }


    public void approve(Employee employee) {
        if (status != StepStatus.PENDING) {
            throw new IllegalStateException("Нельзя согласовать шаг со статусом " + status);
        }
        this.approver = employee;
        this.approvalDate = LocalDate.now();
        this.status = StepStatus.APPROVED;
    }

    public void reject(Employee employee) {
        if (status != StepStatus.PENDING) {
            throw new IllegalStateException("Нельзя отклонить шаг со статусом " + status);
        }
        this.approver = employee;
        this.approvalDate = LocalDate.now();
        this.status = StepStatus.REJECTED;
    }


    public boolean isCompleted() {
        return status == StepStatus.APPROVED || status == StepStatus.REJECTED;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(rule.getDisplayName());
        if (requiredRole != null) {
            sb.append(" (").append(requiredRole.getDisplayName()).append(")");
        }
        sb.append(" — ").append(status.getDisplayName());
        if (approver != null) {
            sb.append(" [").append(approver.getFullName()).append(", ").append(approvalDate).append("]");
        }
        return sb.toString();
    }
}