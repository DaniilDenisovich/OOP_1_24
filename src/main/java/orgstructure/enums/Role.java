package orgstructure.enums;
/**
  Роли сотрудников в организации.
  Перечисление с полем displayName для читаемого отображения.
 */
public enum Role {
    DIRECTOR("Директор"),
    DEPARTMENT_HEAD("Руководитель подразделения"),
    ACCOUNTANT("Бухгалтер"),
    LAWYER("Юрист"),
    ENGINEER("Инженер"),
    MANAGER("Менеджер");

    private final String displayName;

    Role(String displayName) {
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