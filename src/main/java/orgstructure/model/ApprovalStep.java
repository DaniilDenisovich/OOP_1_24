package orgstructure.model;

import orgstructure.enums.ApproverRule;
import orgstructure.enums.Role;
import orgstructure.enums.StepStatus;
import java.time.LocalDate;

/**
  Шаг маршрута согласования документа.
  Определяет, кто должен согласовать документ, текущий статус и историю.
 */
public class ApprovalStep {
    private final ApproverRule rule;
    private final Role requiredRole;
    private StepStatus status;
    private Employee approver;
    private Employee expectedApprover;
    private LocalDate approvalDate;
    private final LocalDate createdDate;

    /**
      Создаёт шаг маршрута с указанным правилом выбора согласующего.
      @param rule правило выбора согласующего
      @param requiredRole требуемая роль
      @throws IllegalArgumentException если rule = ROLE_HOLDER, но requiredRole = null
     */
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

    /** @return правило выбора согласующего */
    public ApproverRule getRule() {
        return rule;
    }

    /** @return требуемая роль или null */
    public Role getRequiredRole() {
        return requiredRole;
    }

    /** @return текущий статус шага */
    public StepStatus getStatus() {
        return status;
    }

    /** @return согласующий или null, если шаг ещё не завершён */
    public Employee getApprover() {
        return approver;
    }

    /** @return ожидаемый согласующий или null */
    public Employee getExpectedApprover() {
        return expectedApprover;
    }

    /**
      Устанавливает ожидаемого согласующего.
      @param expectedApprover сотрудник, который должен согласовать
     */
    public void setExpectedApprover(Employee expectedApprover) {
        this.expectedApprover = expectedApprover;
    }

    /**
      Согласует шаг указанным сотрудником.
      @param employee сотрудник, который согласует
      @throws IllegalStateException если шаг уже завершён
     */
    public void approve(Employee employee) {
        if (status != StepStatus.PENDING) {
            throw new IllegalStateException("Нельзя согласовать шаг со статусом " + status);
        }
        this.approver = employee;
        this.approvalDate = LocalDate.now();
        this.status = StepStatus.APPROVED;
    }

    /**
      Отклоняет шаг указанным сотрудником.
      @param employee сотрудник, который отклоняет
      @throws IllegalStateException если шаг уже завершён
     */
    public void reject(Employee employee) {
        if (status != StepStatus.PENDING) {
            throw new IllegalStateException("Нельзя отклонить шаг со статусом " + status);
        }
        this.approver = employee;
        this.approvalDate = LocalDate.now();
        this.status = StepStatus.REJECTED;
    }

    /**
      Проверяет, завершён ли шаг.
      @return true, если шаг завершён
     */
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