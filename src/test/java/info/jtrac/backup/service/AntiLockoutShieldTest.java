package info.jtrac.backup.service;

import info.jtrac.backup.model.SystemBackupData;
import info.jtrac.backup.model.dto.UserDto;
import info.jtrac.backup.model.dto.UserSpaceRoleDto;
import info.jtrac.domain.Role;
import info.jtrac.domain.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AntiLockoutShieldTest {

    @Test
    public void testShieldExistingUserRetainsIdAndProtectsPassword() {
        BackupRestoreService service = new BackupRestoreService();

        SystemBackupData data = new SystemBackupData();

        UserDto oldAdmin = new UserDto();
        oldAdmin.setId(10L);
        oldAdmin.setLoginName("william");
        oldAdmin.setPassword("old_forgotten_hash_123");
        oldAdmin.setName("William Old");
        data.getUsers().add(oldAdmin);

        User currentOperator = new User();
        currentOperator.setId(999L);
        currentOperator.setLoginName("william");
        currentOperator.setPassword("active_current_password_hash_456");
        currentOperator.setName("William Current");

        service.applyAntiLockoutShield(data, currentOperator);

        assertEquals(1, data.getUsers().size());
        UserDto shielded = data.getUsers().get(0);
        // ID must be 10L to preserve historical item foreign keys
        assertEquals(10L, shielded.getId());
        // Password MUST be current operator's active password
        assertEquals("active_current_password_hash_456", shielded.getPassword());
        assertFalse(shielded.isLocked());

        // Must have ROLE_ADMIN
        boolean hasAdmin = data.getUserSpaceRoles().stream()
                .anyMatch(r -> r.getUserId() == 10L && r.getSpaceId() == null && Role.ROLE_ADMIN.equals(r.getRoleKey()));
        assertTrue(hasAdmin, "Shielded user must have global ROLE_ADMIN");
    }

    @Test
    public void testShieldMissingUserInjectsSuperUser() {
        BackupRestoreService service = new BackupRestoreService();

        SystemBackupData data = new SystemBackupData();

        UserDto otherUser = new UserDto();
        otherUser.setId(1L);
        otherUser.setLoginName("legacy_admin");
        otherUser.setPassword("legacy_pass");
        data.getUsers().add(otherUser);

        User currentOperator = new User();
        currentOperator.setLoginName("new_ops");
        currentOperator.setPassword("ops_secure_pass");
        currentOperator.setName("Ops Admin");
        currentOperator.setEmail("ops@example.com");

        service.applyAntiLockoutShield(data, currentOperator);

        // Should now have 2 users
        assertEquals(2, data.getUsers().size());

        UserDto legacy = data.getUsers().stream().filter(u -> "legacy_admin".equals(u.getLoginName())).findFirst().orElse(null);
        assertNotNull(legacy);
        assertEquals("legacy_pass", legacy.getPassword());

        UserDto injected = data.getUsers().stream().filter(u -> "new_ops".equals(u.getLoginName())).findFirst().orElse(null);
        assertNotNull(injected);
        assertEquals("ops_secure_pass", injected.getPassword());
        assertEquals("Ops Admin", injected.getName());
        assertEquals("ops@example.com", injected.getEmail());

        // Injected user must have ROLE_ADMIN
        boolean hasAdmin = data.getUserSpaceRoles().stream()
                .anyMatch(r -> r.getUserId() == injected.getId() && r.getSpaceId() == null && Role.ROLE_ADMIN.equals(r.getRoleKey()));
        assertTrue(hasAdmin, "Injected user must have global ROLE_ADMIN");
    }
}
