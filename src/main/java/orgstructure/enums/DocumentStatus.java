package orgstructure.enums;

public enum DocumentStatus {
    DRAFT("Черновик", false),
    IN_PROGRESS("На согласовании", false),
    APPROVED("Согласован", true),
    REJECTED("Отклонён", true);

    private final String title;
    private final boolean terminal;

    DocumentStatus(String title, boolean terminal) {
        this.title = title;
        this.terminal = terminal;
    }

    public String getTitle() {
        return title;
    }

    public boolean isTerminal() {
        return terminal;
    }
}