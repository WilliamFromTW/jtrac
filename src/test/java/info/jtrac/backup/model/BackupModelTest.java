package info.jtrac.backup.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import info.jtrac.backup.model.dto.*;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class BackupModelTest {

    @Test
    public void testManifestSerialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        BackupManifest manifest = new BackupManifest();
        manifest.setJtracVersion("2.3.3");
        manifest.setSchemaVersion("1.0");
        manifest.setBackupTimestamp(new Date());
        manifest.setOperatorLoginName("admin");
        manifest.setDatabaseDialect("HSQLDialect");
        manifest.getTableCounts().put("users", 3);
        manifest.getTableCounts().put("items", 15);
        manifest.setTotalAttachmentFiles(2);
        manifest.setTotalAttachmentBytes(1024L);

        String json = mapper.writeValueAsString(manifest);
        assertNotNull(json);
        assertTrue(json.contains("\"jtracVersion\":\"2.3.3\""));
        assertTrue(json.contains("\"operatorLoginName\":\"admin\""));

        BackupManifest readBack = mapper.readValue(json, BackupManifest.class);
        assertEquals("2.3.3", readBack.getJtracVersion());
        assertEquals("admin", readBack.getOperatorLoginName());
        assertEquals(3, readBack.getTableCounts().get("users"));
        assertEquals(15, readBack.getTableCounts().get("items"));
        assertEquals(1024L, readBack.getTotalAttachmentBytes());
    }

    @Test
    public void testSystemBackupDataSerialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        SystemBackupData data = new SystemBackupData();
        data.getConfigs().add(new ConfigDto("mail.server.host", "smtp.example.com"));

        SpaceDto space = new SpaceDto();
        space.setId(1L);
        space.setPrefixCode("TEST");
        space.setName("Test Space");
        data.getSpaces().add(space);

        UserDto user = new UserDto();
        user.setId(2L);
        user.setLoginName("tester");
        user.setName("Test User");
        user.setPassword("secretHash");
        data.getUsers().add(user);

        ItemDto item = new ItemDto();
        item.setId(10L);
        item.setSpaceId(1L);
        item.setSequenceNum(1L);
        item.setLoggedById(2L);
        item.setSummary("Bug summary");
        item.setDetail("Bug detail");
        item.setTimeStamp(new Date());
        data.getItems().add(item);

        HistoryDto history = new HistoryDto();
        history.setId(20L);
        history.setItemId(10L);
        history.setLoggedById(2L);
        history.setComment("Fixed the bug");
        history.setTimeStamp(new Date());
        data.getHistories().add(history);

        String json = mapper.writeValueAsString(data);
        assertNotNull(json);
        assertTrue(json.contains("\"prefixCode\":\"TEST\""));
        assertTrue(json.contains("\"summary\":\"Bug summary\""));

        SystemBackupData readBack = mapper.readValue(json, SystemBackupData.class);
        assertEquals(1, readBack.getConfigs().size());
        assertEquals("smtp.example.com", readBack.getConfigs().get(0).getValue());
        assertEquals(1, readBack.getSpaces().size());
        assertEquals("TEST", readBack.getSpaces().get(0).getPrefixCode());
        assertEquals(1, readBack.getUsers().size());
        assertEquals("tester", readBack.getUsers().get(0).getLoginName());
        assertEquals(1, readBack.getItems().size());
        assertEquals("Bug summary", readBack.getItems().get(0).getSummary());
        assertEquals(1, readBack.getHistories().size());
        assertEquals("Fixed the bug", readBack.getHistories().get(0).getComment());
    }
}
