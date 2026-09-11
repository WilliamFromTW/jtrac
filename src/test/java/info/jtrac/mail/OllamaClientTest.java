package info.jtrac.mail;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class OllamaClientTest {

    @Test
    public void testUrlNormalizationAndDefaults() {
        OllamaClient client = new OllamaClient("http://localhost:11434/", "qwen2.5", "token123", 45);
        assertEquals("http://localhost:11434", client.getBaseUrl());
        assertEquals("qwen2.5", client.getModel());
        assertEquals("token123", client.getApiKey());
        assertEquals(45, client.getTimeoutSeconds());

        OllamaClient defaultClient = new OllamaClient(null, null, "", 0);
        assertEquals("http://localhost:11434", defaultClient.getBaseUrl());
        assertEquals("llama3.2", defaultClient.getModel());
        assertNull(defaultClient.getApiKey());
        assertEquals(60, defaultClient.getTimeoutSeconds());
    }

    @Test
    public void testBuildChatPayload() {
        OllamaClient client = new OllamaClient("http://localhost:11434", "llama3.2", null, 30);
        String payload = client.buildChatPayload("You are assistant", "What is ticket 123?");

        assertTrue(payload.contains("\"model\":\"llama3.2\""));
        assertTrue(payload.contains("\"stream\":false"));
        assertTrue(payload.contains("\"role\":\"system\""));
        assertTrue(payload.contains("\"content\":\"You are assistant\""));
        assertTrue(payload.contains("\"role\":\"user\""));
        assertTrue(payload.contains("\"content\":\"What is ticket 123?\""));
    }

    @Test
    public void testParseValidChatResponse() throws Exception {
        OllamaClient client = new OllamaClient(null, null, null, 10);
        String json = "{\n"
                + "  \"model\": \"llama3.2\",\n"
                + "  \"created_at\": \"2026-09-11T09:00:00Z\",\n"
                + "  \"message\": {\n"
                + "    \"role\": \"assistant\",\n"
                + "    \"content\": \"Ticket [PROJ-123] is currently in progress.\"\n"
                + "  },\n"
                + "  \"done\": true\n"
                + "}";

        String result = client.parseResponse(json);
        assertEquals("Ticket [PROJ-123] is currently in progress.", result);
    }

    @Test(expected = IOException.class)
    public void testParseErrorResponse() throws Exception {
        OllamaClient client = new OllamaClient(null, null, null, 10);
        String json = "{\"error\": \"model 'unknown' not found\"}";
        client.parseResponse(json);
    }

    @Test
    public void testHttpChatWithMockServer() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        int port = server.getAddress().getPort();
        server.createContext("/api/chat", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
                assertEquals("Bearer mock-secret-token", authHeader);
                assertEquals("POST", exchange.getRequestMethod());

                String respJson = "{\"message\": {\"role\": \"assistant\", \"content\": \"All 5 tickets are closed.\"}}";
                byte[] bytes = respJson.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            }
        });
        server.start();

        try {
            OllamaClient client = new OllamaClient("http://localhost:" + port, "llama3.2", "mock-secret-token", 5);
            String response = client.chat("System", "User query");
            assertEquals("All 5 tickets are closed.", response);
        } finally {
            server.stop(0);
        }
    }
}
