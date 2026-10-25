package orgstructure.exception;

public class ApprovalException extends Exception {

    public ApprovalException(String message) {
        super(message);
    }

    public ApprovalException(String message, Throwable cause) {
        super(message, cause);
    }
}