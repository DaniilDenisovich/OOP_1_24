package orgstructure.model;

import orgstructure.enums.StepStatus;

import java.time.LocalDateTime;
import java.util.Objects;

public record ApprovalRecord(
        Employee approver,
        StepStatus status,
        String comment,
        LocalDateTime timestamp
) {
    public ApprovalRecord {
        Objects.requireNonNull(approver, "Согласующий не может быть null");
        Objects.requireNonNull(status, "Статус не может быть null");
        Objects.requireNonNull(timestamp, "Временная метка не может быть null");
        if (comment == null) {
            comment = "";
        }
    }
}