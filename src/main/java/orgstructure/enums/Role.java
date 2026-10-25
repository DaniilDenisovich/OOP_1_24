package orgstructure.enums;

/**
 * Роли сотрудников в организации.
 * Перечисление с полями для наглядного отображения должности.
 */
public enum Role {
    DIRECTOR("Генеральный директор"),
    DEPARTMENT_HEAD("Руководитель департамента"),
    TEAM_LEAD("Руководитель группы / Тимлид"),
    DEVELOPER("Разработчик"),
    ACCOUNTANT("Бухгалтер"),
    LAWYER("Юрист"),
    HR("Специалист по кадрам"),
    EMPLOYEE("Сотрудник");

    private final String title;

    Role(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return title;
    }
}