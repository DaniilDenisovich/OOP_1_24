package orgstructure.exception;

/**
 * Исключение, возникающее при попытке выполнить недопустимую операцию согласования.
 * Например: попытка согласовать шаг, предназначенный другому сотруднику,
 * или согласование уже завершённого шага.
 */

public class ApprovalException extends RuntimeException {

    /**
     * Создаёт исключение с сообщением.
     *
     * @param message описание причины ошибки
     */

    public ApprovalException(String message) {
        super(message);
    }
}