package com.docspace.service;

import com.docspace.exception.ForbiddenException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Exports a document's TipTap JSON content to Markdown or PDF. Both walk the same
 * node tree — Markdown emits text directly, PDF renders a small intermediate HTML
 * string and feeds it to openhtmltopdf (pure-Java, no headless browser dependency).
 */
@Service
public class ExportService {

    private final DocumentService documentService;
    private final ObjectMapper objectMapper;

    public ExportService(DocumentService documentService, ObjectMapper objectMapper) {
        this.documentService = documentService;
        this.objectMapper = objectMapper;
    }

    public ExportedDocument toMarkdown(UUID documentId, UUID callerId) {
        var access = documentService.resolveAccess(documentId, callerId);
        if (!access.canRead()) {
            throw new ForbiddenException("You do not have access to export this document");
        }

        JsonNode content = parseContent(access.document().getContent());
        StringBuilder markdown = new StringBuilder();
        for (JsonNode block : content.path("content")) {
            appendMarkdownBlock(markdown, block);
        }

        return new ExportedDocument(access.document().getTitle(), markdown.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    public ExportedDocument toPdf(UUID documentId, UUID callerId) {
        var access = documentService.resolveAccess(documentId, callerId);
        if (!access.canRead()) {
            throw new ForbiddenException("You do not have access to export this document");
        }

        JsonNode content = parseContent(access.document().getContent());
        StringBuilder html = new StringBuilder();
        html.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><meta charset=\"utf-8\"/><style>")
                .append("body{font-family:sans-serif;padding:32px;color:#1a2420;}")
                .append("h1{font-size:28px;margin:16px 0 8px;} h2{font-size:20px;margin:14px 0 6px;}")
                .append("p{margin:6px 0;line-height:1.5;} ul,ol{margin:6px 0;padding-left:24px;}")
                .append("</style></head><body>");
        for (JsonNode block : content.path("content")) {
            appendHtmlBlock(html, block);
        }
        html.append("</body></html>");

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html.toString(), null);
            builder.toStream(out);
            builder.run();
            return new ExportedDocument(access.document().getTitle(), out.toByteArray());
        } catch (IOException ex) {
            throw new RuntimeException("Could not render PDF export", ex);
        }
    }

    private void appendMarkdownBlock(StringBuilder out, JsonNode block) {
        String type = block.path("type").asText();
        switch (type) {
            case "heading" -> {
                int level = block.path("attrs").path("level").asInt(1);
                out.append("#".repeat(Math.max(1, level))).append(' ').append(textOf(block)).append("\n\n");
            }
            case "bulletList" -> {
                for (JsonNode item : block.path("content")) {
                    out.append("- ").append(textOfListItem(item)).append('\n');
                }
                out.append('\n');
            }
            case "orderedList" -> {
                int i = 1;
                for (JsonNode item : block.path("content")) {
                    out.append(i++).append(". ").append(textOfListItem(item)).append('\n');
                }
                out.append('\n');
            }
            default -> out.append(textOf(block)).append("\n\n");
        }
    }

    private void appendHtmlBlock(StringBuilder out, JsonNode block) {
        String type = block.path("type").asText();
        switch (type) {
            case "heading" -> {
                int level = Math.min(6, Math.max(1, block.path("attrs").path("level").asInt(1)));
                out.append("<h").append(level).append('>').append(escapeHtml(textOf(block))).append("</h").append(level).append('>');
            }
            case "bulletList" -> {
                out.append("<ul>");
                for (JsonNode item : block.path("content")) {
                    out.append("<li>").append(escapeHtml(textOfListItem(item))).append("</li>");
                }
                out.append("</ul>");
            }
            case "orderedList" -> {
                out.append("<ol>");
                for (JsonNode item : block.path("content")) {
                    out.append("<li>").append(escapeHtml(textOfListItem(item))).append("</li>");
                }
                out.append("</ol>");
            }
            default -> out.append("<p>").append(escapeHtml(textOf(block))).append("</p>");
        }
    }

    private String textOf(JsonNode block) {
        StringBuilder sb = new StringBuilder();
        for (JsonNode child : block.path("content")) {
            if (child.has("text")) {
                sb.append(child.get("text").asText());
            }
        }
        return sb.toString();
    }

    private String textOfListItem(JsonNode listItem) {
        StringBuilder sb = new StringBuilder();
        for (JsonNode paragraph : listItem.path("content")) {
            sb.append(textOf(paragraph));
        }
        return sb.toString();
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private JsonNode parseContent(String raw) {
        try {
            return objectMapper.readTree(raw);
        } catch (Exception ex) {
            return objectMapper.createObjectNode();
        }
    }

    public record ExportedDocument(String title, byte[] bytes) {
    }
}
