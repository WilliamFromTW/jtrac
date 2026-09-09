package info.jtrac.backup.service;

import info.jtrac.JtracTestBase;
import info.jtrac.backup.model.BackupManifest;
import info.jtrac.backup.model.SystemBackupData;
import info.jtrac.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class BackupRestoreServiceIntegrationTest extends JtracTestBase {

    @Test
    public void testFullBackupAndRestoreRoundtrip() throws Exception {
        // 1. Clean existing tables
        deleteFromTables(
                "item_tags", "item_users", "item_items", "history", "attachments",
                "items", "user_space_roles", "space_sequence", "spaces",
                "metadata", "storedsearch", "tags", "config"
        );
        deleteFromTables("users");

        // 2. Setup initial data
        User admin = new User();
        admin.setLoginName("backup_admin");
        admin.setName("Backup Administrator");
        admin.setEmail("admin@example.com");
        admin.setPassword(jtrac.encodeClearText("initial_password"));
        admin.addSpaceWithRole(null, Role.ROLE_ADMIN);
        jtrac.storeUser(admin);

        Space space = new Space();
        space.setPrefixCode("BKUP");
        space.setName("Backup Test Space");
        Metadata metadata = new Metadata();
        metadata.setName("Test Metadata");
        metadata.setXmlString("<metadata/>");
        space.setMetadata(metadata);
        jtrac.storeSpace(space);

        admin.addSpaceWithRole(space, Role.ROLE_ADMIN);
        jtrac.storeUser(admin);

        Item item = new Item();
        item.setSpace(space);
        item.setLoggedBy(admin);
        item.setSummary("Important original ticket");
        item.setDetail("Ticket details description");
        item.setTimeStamp(new Date());
        jtrac.storeItem(item, null);

        // Create sample physical attachment & DB link
        File attachDir = new File(jtrac.getJtracHome(), "attachments");
        if (!attachDir.exists()) {
            attachDir.mkdirs();
        }
        File attFile = new File(attachDir, "99999_test_artifact.txt");
        Files.write(attFile.toPath(), "Attachment content before restore".getBytes(StandardCharsets.UTF_8));

        jdbcTemplate.update("insert into attachments (id, file_name, file_prefix) values (?, ?, ?)", 99999L, "test_artifact.txt", 99999L);
        jdbcTemplate.update("update history set attachment_id = ? where item_id = ?", 99999L, item.getId());

        // 3. Export backup ZIP
        SystemBackupData exportData = jtrac.exportSystemData();
        BackupManifest manifest = jtrac.getBackupExportService().createManifest(exportData, "backup_admin");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        jtrac.getZipBundleService().createBackupZip(manifest, exportData, jtrac.getJtracHome(), baos);
        byte[] backupZipBytes = baos.toByteArray();
        assertTrue(backupZipBytes.length > 0, "Exported zip should not be empty");

        // 4. Modify database & disk to simulate drift or corruption
        jdbcTemplate.update("update items set summary = ? where id = ?", "Corrupted ticket summary", item.getId());

        // Simulate operator rotating active password
        admin.setPassword(jtrac.encodeClearText("new_shielded_password"));
        jtrac.storeUser(admin);

        // Remove the attachment from disk to verify it gets restored
        attFile.delete();
        assertFalse(attFile.exists(), "Attachment file should be deleted before restore");

        // 5. Perform Full Restore with operator credentials passed for anti-lockout shielding
        ByteArrayInputStream bais = new ByteArrayInputStream(backupZipBytes);
        jtrac.performFullRestore(bais, admin);

        // 6. Assertions
        // A. Space restored
        Space restoredSpace = jtrac.loadSpace(space.getId());
        assertNotNull(restoredSpace);
        assertEquals("BKUP", restoredSpace.getPrefixCode());

        // B. Item restored with original summary
        Item restoredItem = jtrac.loadItem(item.getId());
        assertNotNull(restoredItem);
        assertEquals("Important original ticket", restoredItem.getSummary());

        // C. Attachment physical file restored
        assertTrue(attFile.exists(), "Attachment file should be restored onto disk");
        String fileContent = new String(Files.readAllBytes(attFile.toPath()), StandardCharsets.UTF_8);
        assertEquals("Attachment content before restore", fileContent);

        // D. Operator credentials shielded (password matches new active password, not the old backup hash)
        User restoredAdmin = jtrac.loadUser("backup_admin");
        assertNotNull(restoredAdmin);
        assertTrue(passwordEncoder.matches("new_shielded_password", restoredAdmin.getPassword()),
                "Operator password should be protected with the active credentials");
        int adminCount = jdbcTemplate.queryForObject(
                "select count(0) from user_space_roles where user_id = ? and space_id is null and role_key = 'ROLE_ADMIN'",
                Integer.class, restoredAdmin.getId());
        assertTrue(adminCount > 0, "Operator should retain SuperUser privileges");

        // E. Safety snapshot created in ${jtrac.home}/backups/
        File backupsDir = new File(jtrac.getJtracHome(), "backups");
        assertTrue(backupsDir.exists() && backupsDir.isDirectory());
        File[] snapshots = backupsDir.listFiles((dir, name) -> name.startsWith("snapshot-before-restore-") && name.endsWith(".zip"));
        assertNotNull(snapshots);
        assertTrue(snapshots.length >= 1, "At least one safety snapshot should have been generated");
    }

    @org.junit.jupiter.api.AfterEach
    public void tearDown() {
        deleteFromTables(
                "item_tags", "item_users", "item_items", "history", "attachments",
                "items", "user_space_roles", "space_sequence", "spaces",
                "metadata", "storedsearch", "tags", "config"
        );
        deleteFromTables("users");

        jdbcTemplate.execute("insert into users (id, login_name, name, email, password, locked, locale) " +
                "values (1, 'admin', 'Admin', 'admin', '21232f297a57a5a743894a0e4a801fc3', false, 'en')");
        jdbcTemplate.execute("insert into user_space_roles (id, user_id, space_id, role_key) " +
                "values (1, 1, null, 'ROLE_ADMIN')");
        try {
            jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 2");
            jdbcTemplate.execute("ALTER TABLE user_space_roles ALTER COLUMN id RESTART WITH 2");
        } catch (Exception ignored) {}
        try {
            if (dao != null) {
                dao.clearSession();
            }
        } catch (Exception ignored) {}
    }
}
