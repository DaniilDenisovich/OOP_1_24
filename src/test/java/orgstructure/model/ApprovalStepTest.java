package orgstructure.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import orgstructure.enums.ApproverRule;
import orgstructure.enums.Role;
import orgstructure.enums.StepStatus;

import static org.junit.jupiter.api.Assertions.*;


class ApprovalStepTest {

    @Test
    @DisplayName("Создание шага с корректными данными")
    void testValidStep() {
        ApprovalStep step = new ApprovalStep(ApproverRule.DIRECT_MANAGER, null);
        assertEquals(ApproverRule.DIRECT_MANAGER, step.getRule());
        assertNull(step.getRequiredRole());
        assertEquals(StepStatus.PENDING, step.getStatus());
        assertNull(step.getApprover());
        assertNull(step.getApprovalDate());
        assertNotNull(step.getCreatedDate());
    }

    @Test
    @DisplayName("Создание шага с null правилом должно выбросить исключение")
    void testNullRule() {
        assertThrows(IllegalArgumentException.class, () -> new ApprovalStep(null, null));
    }

    @Test
    @DisplayName("Создание шага ROLE_HOLDER без requiredRole должно выбросить исключение")
    void testRoleHolderWithoutRequiredRole() {
        assertThrows(IllegalArgumentException.class, () -> new ApprovalStep(ApproverRule.ROLE_HOLDER, null));
    }

    @Test
    @DisplayName("Создание шага ROLE_HOLDER с requiredRole")
    void testRoleHolderWithRequiredRole() {
        ApprovalStep step = new ApprovalStep(ApproverRule.ROLE_HOLDER, Role.LAWYER);
        assertEquals(ApproverRule.ROLE_HOLDER, step.getRule());
        assertEquals(Role.LAWYER, step.getRequiredRole());
    }

    @Test
    @DisplayName("Согласование шага")
    void testApprove() {
        ApprovalStep step = new ApprovalStep(ApproverRule.DIRECT_MANAGER, null);
        Employee approver = new Employee("Иванов И.И.", Role.DIRECTOR);
        step.approve(approver);

        assertEquals(StepStatus.APPROVED, step.getStatus());
        assertEquals(approver, step.getApprover());
        assertNotNull(step.getApprovalDate());
    }

    @Test
    @DisplayName("Отклонение шага")
    void testReject() {
        ApprovalStep step = new ApprovalStep(ApproverRule.DIRECT_MANAGER, null);
        Employee approver = new Employee("Иванов И.И.", Role.DIRECTOR);
        step.reject(approver);

        assertEquals(StepStatus.REJECTED, step.getStatus());
        assertEquals(approver, step.getApprover());
        assertNotNull(step.getApprovalDate());
    }

    @Test
    @DisplayName("Повторное согласование завершённого шага должно выбросить исключение")
    void testApproveCompletedStep() {
        ApprovalStep step = new ApprovalStep(ApproverRule.DIRECT_MANAGER, null);
        Employee approver = new Employee("Иванов И.И.", Role.DIRECTOR);
        step.approve(approver);

        assertThrows(IllegalStateException.class, () -> step.approve(approver));
    }

    @Test
    @DisplayName("Повторное отклонение завершённого шага должно выбросить исключение")
    void testRejectCompletedStep() {
        ApprovalStep step = new ApprovalStep(ApproverRule.DIRECT_MANAGER, null);
        Employee approver = new Employee("Иванов И.И.", Role.DIRECTOR);
        step.reject(approver);

        assertThrows(IllegalStateException.class, () -> step.reject(approver));
    }

    @Test
    @DisplayName("Метод isCompleted возвращает true для согласованного шага")
    void testIsCompletedApproved() {
        ApprovalStep step = new ApprovalStep(ApproverRule.DIRECT_MANAGER, null);
        step.approve(new Employee("Иванов И.И.", Role.DIRECTOR));
        assertTrue(step.isCompleted());
    }

    @Test
    @DisplayName("Метод isCompleted возвращает false для ожидающего шага")
    void testIsCompletedPending() {
        ApprovalStep step = new ApprovalStep(ApproverRule.DIRECT_MANAGER, null);
        assertFalse(step.isCompleted());
    }
}
