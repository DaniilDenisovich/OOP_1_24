package orgstructure.enums;

public enum StepStatus {
    PENDING("Ожидает согласования"),
    APPROVED("Согласован"),
    REJECTED("Отклонён"),
    SKIPPED("Пропущен");

    private final String description;

    StepStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}