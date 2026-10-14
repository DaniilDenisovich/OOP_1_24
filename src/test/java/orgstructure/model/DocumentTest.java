package orgstructure.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import orgstructure.enums.ApproverRule;
import orgstructure.enums.DocumentStatus;
import orgstructure.enums.Role;

import static org.junit.jupiter.api.Assertions.*;

class DocumentTest {

    @Test
    @DisplayName("Создание документа с корректным названием")
    void testValidDocument() {
        Document doc = new Document("Договор поставки");
        assertEquals("Договор поставки", doc.getTitle());
        assertEquals(DocumentStatus.DRAFT, doc.getStatus());
        assertTrue(doc.getSteps().isEmpty());
        assertFalse(doc.getHistory().isEmpty());
        assertNotNull(doc.getCreatedDate());
    }

    @Test
    @DisplayName("Создание документа с null названием должно выбросить исключение")
    void testNullTitle() {
        assertThrows(IllegalArgumentException.class, () -> new Document(null));
    }

    @Test
    @DisplayName("Создание документа с пустым названием должно выбросить исключение")
    void testEmptyTitle() {
        assertThrows(IllegalArgumentException.class, () -> new Document("   "));
    }

    @Test
    @DisplayName("Добавление шага в документ в статусе DRAFT")
    void testAddStepInDraft() {
        Document doc = new Document("Договор");
        ApprovalStep step = new ApprovalStep(ApproverRule.DIRECT_MANAGER, null);
        doc.addStep(step);
        assertEquals(1, doc.getSteps().size());
    }

    @Test
    @DisplayName("Добавление шага в документ не в статусе DRAFT должно выбросить исключение")
    void testAddStepNotInDraft() {
        Document doc = new Document("Договор");
        doc.addStep(new ApprovalStep(ApproverRule.DIRECT_MANAGER, null));
        doc.startApproval();

        ApprovalStep step2 = new ApprovalStep(ApproverRule.DEPARTMENT_HEAD, null);
        assertThrows(IllegalStateException.class, () -> doc.addStep(step2));
    }

    @Test
    @DisplayName("Запуск согласования без шагов должен выбросить исключение")
    void testStartApprovalWithoutSteps() {
        Document doc = new Document("Договор");
        assertThrows(IllegalStateException.class, doc::startApproval);
    }

    @Test
    @DisplayName("Запуск согласования переводит документ в статус IN_APPROVAL")
    void testStartApproval() {
        Document doc = new Document("Договор");
        doc.addStep(new ApprovalStep(ApproverRule.DIRECT_MANAGER, null));
        doc.startApproval();
        assertEquals(DocumentStatus.IN_APPROVAL, doc.getStatus());
    }

    @Test
    @DisplayName("Возвращаемый список шагов неизменяем (Инкапсуляция)")
    void testUnmodifiableStepsList() {
        Document doc = new Document("Договор");
        doc.addStep(new ApprovalStep(ApproverRule.DIRECT_MANAGER, null));

        assertThrows(UnsupportedOperationException.class, () -> {
            doc.getSteps().add(new ApprovalStep(ApproverRule.DEPARTMENT_HEAD, null));
        });
    }

    @Test
    @DisplayName("Возвращаемый список истории неизменяем (Инкапсуляция)")
    void testUnmodifiableHistoryList() {
        Document doc = new Document("Договор");
        assertThrows(UnsupportedOperationException.class, () -> {
            doc.getHistory().add("Тестовая запись");
        });
    }

    @Test
    @DisplayName("Обновление статуса при согласовании всех шагов")
    void testUpdateStatusAllApproved() {
        Document doc = new Document("Договор");
        ApprovalStep step1 = new ApprovalStep(ApproverRule.DIRECT_MANAGER, null);
        ApprovalStep step2 = new ApprovalStep(ApproverRule.DEPARTMENT_HEAD, null);
        doc.addStep(step1);
        doc.addStep(step2);
        doc.startApproval();

        Employee approver = new Employee("Иванов И.И.", Role.DIRECTOR);
        step1.approve(approver);
        step2.approve(approver);
        doc.updateStatus();

        assertEquals(DocumentStatus.APPROVED, doc.getStatus());
    }

    @Test
    @DisplayName("Обновление статуса при отклонении хотя бы одного шага")
    void testUpdateStatusAnyRejected() {
        Document doc = new Document("Договор");
        ApprovalStep step1 = new ApprovalStep(ApproverRule.DIRECT_MANAGER, null);
        ApprovalStep step2 = new ApprovalStep(ApproverRule.DEPARTMENT_HEAD, null);
        doc.addStep(step1);
        doc.addStep(step2);
        doc.startApproval();

        Employee approver = new Employee("Иванов И.И.", Role.DIRECTOR);
        step1.approve(approver);
        step2.reject(approver);
        doc.updateStatus();

        assertEquals(DocumentStatus.REJECTED, doc.getStatus());
    }
}