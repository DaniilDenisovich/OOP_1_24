package orgstructure.enums;

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