package orgstructure.model;

import orgstructure.enums.DocumentStatus;
import orgstructure.enums.StepStatus;
import orgstructure.exception.ApprovalException;
import orgstructure.exception.UnauthorizedApproverException;
import orgstructure.service.ApproverRule;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Документ, проходящий маршрут согласования.
 * Хранит автора, статус, маршрут из шагов, историю действий
 * и управляет своим жизненным циклом (построение маршрута, согласование, отклонение).
 */
public class Document {

    private final String id;
    private final String title;
    private final Employee author;
    private final LocalDateTime createdAt;

    private DocumentStatus status;
    private final List<ApprovalStep> route = new ArrayList<>();
    private final List<ApprovalRecord> history = new ArrayList<>();

    public Document(String id, String title, Employee author, LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id, "ID документа не может быть null");
        this.title = Objects.requireNonNull(title, "Название документа не может быть null");
        this.author = Objects.requireNonNull(author, "Автор документа не может быть null");
        this.createdAt = Objects.requireNonNull(createdAt, "Дата создания не может быть null");
        this.status = DocumentStatus.DRAFT;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Employee getAuthor() {
        return author;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = Objects.requireNonNull(status, "Статус документа не может быть null");
    }

    /**
     * Устанавливает маршрут согласования для документа.
     * Защита инварианта: маршрут задается только пока документ в статусе DRAFT.
     */
    public void setRoute(List<ApprovalStep> steps) {
        Objects.requireNonNull(steps, "Список шагов не может быть null");
        if (this.status != DocumentStatus.DRAFT) {
            throw new IllegalStateException("Нельзя менять маршрут документа после запуска согласования");
        }
        this.route.clear();
        this.route.addAll(steps);
    }

    public List<ApprovalStep> getRoute() {
        return Collections.unmodifiableList(route);
    }

    public List<ApprovalRecord> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public void addHistoryRecord(ApprovalRecord record) {
        this.history.add(Objects.requireNonNull(record, "Запись истории не может быть null"));
    }

    public Optional<ApprovalStep> getCurrentStep() {
        return route.stream()
                .filter(step -> step.getStatus() == StepStatus.PENDING)
                .findFirst();
    }

    // =========================================================================
    // БИЗНЕС-ОПЕРАЦИИ ЖИЗНЕННОГО ЦИКЛА ДОКУМЕНТА
    // =========================================================================

    /**
     * Формирует маршрут согласования документа на основе переданных правил.
     *
     * @param rules        последовательность правил выбора согласующих
     * @param organization корневая организация для поиска сотрудников
     * @param timeLimit    лимит времени на каждый шаг
     * @param now          момент времени запуска маршрута
     * @throws ApprovalException если согласующий не может быть найден или список правил пуст
     */
    public void buildRoute(List<ApproverRule> rules, Department organization, Duration timeLimit, LocalDateTime now)
            throws ApprovalException {
        Objects.requireNonNull(rules, "Список правил не может быть null");
        Objects.requireNonNull(organization, "Организация не может быть null");
        if (rules.isEmpty()) {
            throw new ApprovalException("Маршрут должен содержать хотя бы одно правило согласования");
        }

        List<ApprovalStep> steps = new ArrayList<>();
        int stepNumber = 1;

        for (ApproverRule rule : rules) {
            Employee approver = rule.resolveApprover(this.author, organization);
            steps.add(new ApprovalStep(stepNumber++, approver, timeLimit, now));
        }

        setRoute(steps);
        this.status = DocumentStatus.IN_PROGRESS;
    }

    /**
     * Согласование текущего шага.
     *
     * @param actor   сотрудник, выполняющий согласование
     * @param comment комментарий
     * @param now     момент времени согласования
     * @throws UnauthorizedApproverException если сотрудник не имеет права согласовывать данный шаг
     */
    public void approve(Employee actor, String comment, LocalDateTime now) {
        validateInProgress();

        ApprovalStep currentStep = getCurrentStep()
                .orElseThrow(() -> new IllegalStateException("Нет активного шага для согласования"));

        if (!currentStep.getApprover().equals(actor)) {
            throw new UnauthorizedApproverException("Сотрудник " + actor.getFullName()
                    + " не имеет права согласовывать шаг #" + currentStep.getStepNumber()
                    + ". Ожидается: " + currentStep.getApprover().getFullName());
        }

        currentStep.setStatus(StepStatus.APPROVED);
        addHistoryRecord(new ApprovalRecord(actor, StepStatus.APPROVED, comment, now));

        if (getCurrentStep().isEmpty()) {
            this.status = DocumentStatus.APPROVED;
        }
    }

    /**
     * Отклонение документа на текущем шаге.
     *
     * @param actor  сотрудник, отклоняющий документ
     * @param reason причина отклонения
     * @param now    момент времени отклонения
     * @throws UnauthorizedApproverException если сотрудник не имеет права отклонять данный шаг
     */
    public void reject(Employee actor, String reason, LocalDateTime now) {
        validateInProgress();

        ApprovalStep currentStep = getCurrentStep()
                .orElseThrow(() -> new IllegalStateException("Нет активного шага для отклонения"));

        if (!currentStep.getApprover().equals(actor)) {
            throw new UnauthorizedApproverException("Сотрудник " + actor.getFullName()
                    + " не имеет права отклонять шаг #" + currentStep.getStepNumber()
                    + ". Ожидается: " + currentStep.getApprover().getFullName());
        }

        currentStep.setStatus(StepStatus.REJECTED);
        addHistoryRecord(new ApprovalRecord(actor, StepStatus.REJECTED, reason, now));
        this.status = DocumentStatus.REJECTED;
    }

    private void validateInProgress() {
        if (this.status != DocumentStatus.IN_PROGRESS) {
            throw new IllegalStateException("Документ #" + id
                    + " не находится на согласовании (текущий статус: " + status + ")");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Document document)) return false;
        return Objects.equals(id, document.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Документ #" + id + " \"" + title + "\" [" + status.getTitle() + "] от " + author.getFullName();
    }
}