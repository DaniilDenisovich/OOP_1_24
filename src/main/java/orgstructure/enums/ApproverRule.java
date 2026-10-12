package orgstructure.enums;

public enum ApproverRule {
    DIRECT_MANAGER("Непосредственный руководитель"),
    DEPARTMENT_HEAD("Руководитель подразделения"),
    ROLE_HOLDER("Сотрудник с ролью");

    private final String displayName;

    ApproverRule(String displayName) {
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