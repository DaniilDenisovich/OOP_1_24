package orgstructure.enums;

/**
  Статусы жизненного цикла документа.
  Реализует конечный автомат, контролирующий допустимые переходы и операции над документом.
 */

public enum DocumentStatus {
    DRAFT("Черновик"),
    IN_APPROVAL("На согласовании"),
    APPROVED("Согласован"),
    REJECTED("Отклонён");

    private final String displayName;

    DocumentStatus(String displayName) {
        this.displayName = displayName;
    }


    public String getDisplayName() {
        return displayName;
    }

    public boolean isTerminal() {
        return this == APPROVED || this == REJECTED;
    }

    @Override
    public String toString() {
        return displayName;
    }
}