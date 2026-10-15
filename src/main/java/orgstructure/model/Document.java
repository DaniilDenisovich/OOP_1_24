package orgstructure.model;

import orgstructure.enums.DocumentStatus;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Документ, проходящий процедуру согласования.
 * Содержит маршрут согласования из шагов и историю изменений статуса.
 */
public class Document {
    private final String title;
    private DocumentStatus status;
    private final List<ApprovalStep> steps;
    private final List<String> history;
    private final LocalDate createdDate;

    /**
     * Создаёт документ с указанным названием в статусе черновика.
     * @param title название документа (не должно быть null или пустым)
     * @throws IllegalArgumentException если title null или пустой
     */
    public Document(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Название документа не может быть пустым");
        }
        this.title = title;
        this.status = DocumentStatus.DRAFT;
        this.steps = new ArrayList<>();
        this.history = new ArrayList<>();
        this.createdDate = LocalDate.now();
        addHistory("Документ создан");
    }

    /** @return название документа */
    public String getTitle() {
        return title;
    }

    /** @return текущий статус документа */
    public DocumentStatus getStatus() {
        return status;
    }

    /**
     * Возвращает список шагов маршрута согласования.
     * @return неизменяемая копия списка для сохранения инкапсуляции
     */
    public List<ApprovalStep> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    /**
     * Возвращает историю изменений документа.
     * @return неизменяемая копия списка записей истории
     */
    public List<String> getHistory() {
        return Collections.unmodifiableList(history);
    }

    /** @return дата создания документа */
    public LocalDate getCreatedDate() {
        return createdDate;
    }

    /**
     * Добавляет шаг в маршрут согласования.
     * @param step шаг для добавления
     * @throws IllegalStateException если документ не в статусе DRAFT
     */
    public void addStep(ApprovalStep step) {
        if (status != DocumentStatus.DRAFT) {
            throw new IllegalStateException("Нельзя добавлять шаги к документу в статусе " + status);
        }
        steps.add(step);
    }

    /**
     * Запускает процесс согласования.
     * @throws IllegalStateException если документ не в статусе DRAFT или нет шагов
     */
    public void startApproval() {
        if (status != DocumentStatus.DRAFT) {
            throw new IllegalStateException("Нельзя начать согласование для документа в статусе " + status);
        }
        if (steps.isEmpty()) {
            throw new IllegalStateException("Нельзя начать согласование без шагов маршрута");
        }
        status = DocumentStatus.IN_APPROVAL;
        addHistory("Начато согласование");
    }

    /**
     * Возвращает текущий шаг, ожидающий согласования.
     * @return текущий шаг или null, если все шаги завершены
     */
    public ApprovalStep getCurrentStep() {
        for (ApprovalStep step : steps) {
            if (step.getStatus() == orgstructure.enums.StepStatus.PENDING) {
                return step;
            }
        }
        return null;
    }

    /**
     * Обновляет статус документа на основе завершения шагов.
     */
    public void updateStatus() {
        boolean allApproved = true;
        boolean anyRejected = false;

        for (ApprovalStep step : steps) {
            if (step.getStatus() == orgstructure.enums.StepStatus.REJECTED) {
                anyRejected = true;
                break;
            }
            if (step.getStatus() != orgstructure.enums.StepStatus.APPROVED) {
                allApproved = false;
            }
        }

        if (anyRejected) {
            status = DocumentStatus.REJECTED;
            addHistory("Документ отклонён");
        } else if (allApproved) {
            status = DocumentStatus.APPROVED;
            addHistory("Документ согласован");
        }
    }

    private void addHistory(String message) {
        history.add(LocalDate.now() + ": " + message);
    }

    @Override
    public String toString() {
        return title + " [" + status.getDisplayName() + "]";
    }
}