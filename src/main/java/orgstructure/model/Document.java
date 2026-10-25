package orgstructure.model;

import orgstructure.enums.DocumentStatus;
import orgstructure.enums.StepStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


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