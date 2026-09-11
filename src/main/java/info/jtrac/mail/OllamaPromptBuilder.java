package info.jtrac.mail;

import info.jtrac.domain.History;
import info.jtrac.domain.Item;

import java.util.List;

/**
 * Constructs structured prompts for Ollama AI Query Copilot.
 * Injects user query, ticket metadata, comments, and extracted attachment text.
 */
public class OllamaPromptBuilder {

    public static final String DEFAULT_SYSTEM_PROMPT =
            "You are JTrac AI Query Copilot (JTrac 郵件 AI 查詢秘書), an intelligent enterprise assistant for the JTrac issue tracking system.\n"
            + "Your role is to analyze the user inquiry and synthesize an accurate, professional, and well-structured response based strictly on the provided context of authorized tickets and document attachments.\n"
            + "Guidelines:\n"
            + "1. Always reply in the same language as the user's inquiry (defaulting to Traditional Chinese if ambiguous).\n"
            + "2. Format your response using clean Markdown with headings, bullet points, and clear sections.\n"
            + "3. Whenever you refer to a ticket, cite its reference ID in brackets, e.g. [PROJ-123], so it can be easily recognized.\n"
            + "4. If the provided tickets and attachments do not contain sufficient info to answer the question, clearly state what was found and what is missing.\n"
            + "5. Never fabricate ticket details or status that does not exist in the provided context.";

    public static String buildSystemPrompt() {
        return DEFAULT_SYSTEM_PROMPT;
    }

    public static String buildUserPrompt(String querySubject, String queryBody, List<Item> contextItems) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== USER INQUIRY ===\n");
        if (querySubject != null && !querySubject.trim().isEmpty()) {
            sb.append("Subject: ").append(querySubject.trim()).append("\n");
        }
        sb.append("Content:\n").append(queryBody != null ? queryBody.trim() : "").append("\n\n");

        sb.append("=== AUTHORIZED TICKETS CONTEXT ===\n");
        if (contextItems == null || contextItems.isEmpty()) {
            sb.append("No matching tickets found within the user's authorized spaces.\n");
        } else {
            sb.append("Total relevant tickets found: ").append(contextItems.size()).append(" (max 20)\n\n");
            int idx = 1;
            for (Item item : contextItems) {
                sb.append("--- Ticket #").append(idx++).append(": [").append(item.getRefId()).append("] ---\n");
                if (item.getSpace() != null) {
                    sb.append("Space: ").append(item.getSpace().getName()).append(" (").append(item.getSpace().getPrefixCode()).append(")\n");
                }
                sb.append("Summary: ").append(item.getSummary() != null ? item.getSummary() : "").append("\n");
                sb.append("Status: ").append(item.getStatusValue()).append("\n");
                if (item.getLoggedBy() != null) {
                    sb.append("Reported By: ").append(item.getLoggedBy().getName()).append("\n");
                }
                if (item.getAssignedTo() != null) {
                    sb.append("Assigned To: ").append(item.getAssignedTo().getName()).append("\n");
                }
                if (item.getDetail() != null && !item.getDetail().trim().isEmpty()) {
                    sb.append("Detail: ").append(item.getDetail().trim()).append("\n");
                }

                // Append history / comments
                if (item.getHistory() != null && !item.getHistory().isEmpty()) {
                    sb.append("Comments & Changes:\n");
                    for (History h : item.getHistory()) {
                        if (h.getComment() != null && !h.getComment().trim().isEmpty()) {
                            String author = h.getLoggedBy() != null ? h.getLoggedBy().getName() : "System";
                            sb.append("  - ").append(author).append(": ").append(h.getComment().trim()).append("\n");
                        }
                        if (h.getAttachmentText() != null && !h.getAttachmentText().trim().isEmpty()) {
                            String attName = h.getAttachment() != null ? h.getAttachment().getFileName() : "Attachment";
                            sb.append("  [Attachment Document Content: ").append(attName).append("]:\n");
                            String attSnippet = h.getAttachmentText().trim();
                            if (attSnippet.length() > 2000) {
                                attSnippet = attSnippet.substring(0, 2000) + "... (truncated)";
                            }
                            sb.append(attSnippet).append("\n");
                        }
                    }
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
