package orgstructure.model;

import orgstructure.enums.DocumentStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Document {
    private final String title;
    private DocumentStatus status;
    private final List<ApprovalStep> steps;
    private final List<String> history;
    private final LocalDate createdDate;


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


    public String getTitle() {
        return title;
    }


    public DocumentStatus getStatus() {
        return status;
    }


    public List<ApprovalStep> getSteps() {
        return Collections.unmodifiableList(steps);
    }


    public List<String> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }


    public void addStep(ApprovalStep step) {
        if (status != DocumentStatus.DRAFT) {
            throw new IllegalStateException("Нельзя добавлять шаги к документу в статусе " + status);
        }
        steps.add(step);
    }


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

    public ApprovalStep getCurrentStep() {
        for (ApprovalStep step : steps) {
            if (step.getStatus() == orgstructure.enums.StepStatus.PENDING) {
                return step;
            }
        }
        return null;
    }


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