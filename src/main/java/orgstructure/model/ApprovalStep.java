package orgstructure.model;

import orgstructure.enums.StepStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;


public class ApprovalStep {

    private final int stepNumber;
    private final Employee approver;
    private final Duration timeLimit;
    private final LocalDateTime assignedAt;
    private StepStatus status;


    public ApprovalStep(int stepNumber, Employee approver, Duration timeLimit, LocalDateTime assignedAt) {
        if (stepNumber <= 0) {
            throw new IllegalArgumentException("Номер шага должен быть положительным числом: " + stepNumber);
        }
        this.stepNumber = stepNumber;
        this.approver = Objects.requireNonNull(approver, "Согласующий не может быть null");
        this.timeLimit = Objects.requireNonNull(timeLimit, "Лимит времени не может быть null");
        this.assignedAt = Objects.requireNonNull(assignedAt, "Время назначения не может быть null");
        this.status = StepStatus.PENDING;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public Employee getApprover() {
        return approver;
    }

    public Duration getTimeLimit() {
        return timeLimit;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public StepStatus getStatus() {
        return status;
    }


    public LocalDateTime getDeadline() {
        return assignedAt.plus(timeLimit);
    }


    public boolean isOverdue(LocalDateTime now) {
        return now.isAfter(getDeadline());
    }

    public void setStatus(StepStatus status) {
        this.status = Objects.requireNonNull(status, "Статус не может быть null");
    }

    @Override
    public String toString() {
        return "Шаг #" + stepNumber + " [" + status.getDescription() + "] -> " + approver.getFullName()
                + " (дедлайн: " + getDeadline() + ")";
    }
}