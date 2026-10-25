package orgstructure.service;

import orgstructure.enums.DocumentStatus;
import orgstructure.enums.StepStatus;
import orgstructure.exception.ApprovalException;
import orgstructure.exception.UnauthorizedApproverException;
import orgstructure.model.ApprovalRecord;
import orgstructure.model.ApprovalStep;
import orgstructure.model.Department;
import orgstructure.model.Document;
import orgstructure.model.Employee;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class ApprovalService {

    private final Department rootOrganization;

    public ApprovalService(Department rootOrganization) {
        this.rootOrganization = Objects.requireNonNull(rootOrganization, "Организация не может быть null");
    }


    public void buildRoute(Document document, List<ApproverRule> rules, Duration timeLimit, LocalDateTime now)
            throws ApprovalException {
        Objects.requireNonNull(document, "Документ не может быть null");
        Objects.requireNonNull(rules, "Список правил не может быть null");
        if (rules.isEmpty()) {
            throw new ApprovalException("Маршрут должен содержать хотя бы одно правило согласования");
        }

        List<ApprovalStep> steps = new ArrayList<>();
        int stepNumber = 1;

        for (ApproverRule rule : rules) {
            Employee approver = rule.resolveApprover(document.getAuthor(), rootOrganization);
            steps.add(new ApprovalStep(stepNumber++, approver, timeLimit, now));
        }

        document.setRoute(steps);
        document.setStatus(DocumentStatus.IN_PROGRESS);
    }


    public void approve(Document document, Employee actor, String comment, LocalDateTime now) {
        validateDocumentInProgress(document);

        ApprovalStep currentStep = document.getCurrentStep()
                .orElseThrow(() -> new IllegalStateException("Нет активного шага для согласования"));

        if (!currentStep.getApprover().equals(actor)) {
            throw new UnauthorizedApproverException("Сотрудник " + actor.getFullName()
                    + " не имеет права согласовывать шаг #" + currentStep.getStepNumber()
                    + ". Ожидается: " + currentStep.getApprover().getFullName());
        }

        currentStep.setStatus(StepStatus.APPROVED);
        document.addHistoryRecord(new ApprovalRecord(actor, StepStatus.APPROVED, comment, now));

        if (document.getCurrentStep().isEmpty()) {
            document.setStatus(DocumentStatus.APPROVED);
        }
    }

    public void reject(Document document, Employee actor, String reason, LocalDateTime now) {
        validateDocumentInProgress(document);

        ApprovalStep currentStep = document.getCurrentStep()
                .orElseThrow(() -> new IllegalStateException("Нет активного шага для отклонения"));

        if (!currentStep.getApprover().equals(actor)) {
            throw new UnauthorizedApproverException("Сотрудник " + actor.getFullName()
                    + " не имеет права отклонять шаг #" + currentStep.getStepNumber()
                    + ". Ожидается: " + currentStep.getApprover().getFullName());
        }

        currentStep.setStatus(StepStatus.REJECTED);
        document.addHistoryRecord(new ApprovalRecord(actor, StepStatus.REJECTED, reason, now));
        document.setStatus(DocumentStatus.REJECTED);
    }

    private void validateDocumentInProgress(Document document) {
        Objects.requireNonNull(document, "Документ не может быть null");
        if (document.getStatus() != DocumentStatus.IN_PROGRESS) {
            throw new IllegalStateException("Документ #" + document.getId()
                    + " не находится на согласовании (текущий статус: " + document.getStatus() + ")");
        }
    }
}