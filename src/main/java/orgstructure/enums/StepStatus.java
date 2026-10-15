package orgstructure.enums;

/**
  Статусы шага маршрута согласования.
  Определяют текущее состояние отдельного этапа согласования документа и итогового статуса документа.
 */

public enum StepStatus {
    PENDING("Ожидает"),
    APPROVED("Согласован"),
    REJECTED("Отклонён");

    private final String displayName;

    StepStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}