package com.docspace.service;

import com.docspace.dto.DocumentResponse;
import com.docspace.exception.BadRequestException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class FileImportService {

    private static final long MAX_FILE_SIZE_BYTES = 2L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("txt", "md", "docx");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "text/plain", "text/markdown", "text/x-markdown", "application/octet-stream",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private final DocumentService documentService;
    private final ObjectMapper objectMapper;

    public FileImportService(DocumentService documentService, ObjectMapper objectMapper) {
        this.documentService = documentService;
        this.objectMapper = objectMapper;
    }

    public DocumentResponse importFile(UUID ownerId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("File exceeds the maximum allowed size of 2MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BadRequestException("Uploaded file has no name");
        }

        String extension = extensionOf(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Only .txt, .md, and .docx files are supported");
        }

        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException("Unsupported file content type: " + contentType);
        }

        String title = stripExtension(originalFilename);

        if (extension.equals("docx")) {
            JsonNode content = parseDocx(file);
            return documentService.createFromImport(ownerId, title, content);
        }

        String text = readUtf8Strict(file);
        if (text.isBlank()) {
            throw new BadRequestException("Uploaded file has no content");
        }

        boolean isMarkdown = extension.equals("md");
        JsonNode content = isMarkdown ? parseMarkdown(text) : parsePlainText(text);

        return documentService.createFromImport(ownerId, title, content);
    }

    private JsonNode parseDocx(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            return parseDocxStream(in);
        } catch (IOException ex) {
            throw new BadRequestException("Could not read uploaded .docx file");
        }
    }

    JsonNode parseDocxStream(InputStream in) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("type", "doc");
        ArrayNode content = root.putArray("content");

        try (XWPFDocument document = new XWPFDocument(in)) {
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getRuns().stream()
                        .map(XWPFRun::text)
                        .filter(java.util.Objects::nonNull)
                        .reduce("", String::concat)
                        .strip();

                if (text.isEmpty()) {
                    continue;
                }

                String style = paragraph.getStyle();
                // Word itself (and most generators) mark list items via numbering (numId);
                // some tools (e.g. python-docx) instead only set a "List Bullet"/"List Number"
                // paragraph style without populating numId, so both signals are checked.
                boolean isListItem = paragraph.getNumID() != null
                        || (style != null && style.toLowerCase().contains("list"));

                if (isListItem) {
                    appendOrExtendBulletList(content, text);
                } else if (style != null && style.equalsIgnoreCase("Heading1")) {
                    content.add(headingNode(1, text));
                } else if (style != null && style.equalsIgnoreCase("Heading2")) {
                    content.add(headingNode(2, text));
                } else if (style != null && (style.equalsIgnoreCase("Title") || style.equalsIgnoreCase("Heading")) ) {
                    content.add(headingNode(1, text));
                } else {
                    content.add(paragraphNode(text));
                }
            }
        } catch (IOException ex) {
            throw new BadRequestException("The uploaded file is not a valid .docx document");
        }

        if (content.isEmpty()) {
            throw new BadRequestException("Uploaded file has no content");
        }

        return root;
    }

    private void appendOrExtendBulletList(ArrayNode content, String itemText) {
        JsonNode last = content.isEmpty() ? null : content.get(content.size() - 1);
        ArrayNode items;
        if (last != null && last.isObject() && "bulletList".equals(last.get("type").asText())) {
            items = (ArrayNode) last.get("content");
        } else {
            ObjectNode list = objectMapper.createObjectNode();
            list.put("type", "bulletList");
            items = list.putArray("content");
            content.add(list);
        }
        ObjectNode listItem = objectMapper.createObjectNode();
        listItem.put("type", "listItem");
        listItem.putArray("content").add(paragraphNode(itemText));
        items.add(listItem);
    }

    private String readUtf8Strict(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            var decoder = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);
            return decoder.decode(java.nio.ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException ex) {
            throw new BadRequestException("File must be valid UTF-8 text");
        } catch (IOException ex) {
            throw new BadRequestException("Could not read uploaded file");
        }
    }

    private String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase();
    }

    private String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }

    private JsonNode parsePlainText(String text) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("type", "doc");
        ArrayNode content = root.putArray("content");

        for (String block : splitIntoBlocks(text)) {
            content.add(paragraphNode(block));
        }

        if (content.isEmpty()) {
            content.add(paragraphNode(""));
        }
        return root;
    }

    private JsonNode parseMarkdown(String text) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("type", "doc");
        ArrayNode content = root.putArray("content");

        for (String block : splitIntoBlocks(text)) {
            String trimmed = block.strip();
            if (trimmed.startsWith("## ")) {
                content.add(headingNode(2, trimmed.substring(3).strip()));
            } else if (trimmed.startsWith("# ")) {
                content.add(headingNode(1, trimmed.substring(2).strip()));
            } else if (isBulletList(trimmed)) {
                content.add(bulletListNode(trimmed));
            } else {
                content.add(paragraphNode(block));
            }
        }

        if (content.isEmpty()) {
            content.add(paragraphNode(""));
        }
        return root;
    }

    private boolean isBulletList(String block) {
        return block.lines().allMatch(line -> line.strip().startsWith("- ") || line.strip().startsWith("* "));
    }

    private List<String> splitIntoBlocks(String text) {
        return List.of(text.replace("\r\n", "\n").split("\n{2,}"));
    }

    private ObjectNode paragraphNode(String text) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("type", "paragraph");
        String flattened = text.strip();
        if (!flattened.isEmpty()) {
            ArrayNode content = node.putArray("content");
            content.add(textNode(flattened.replace("\n", " ")));
        }
        return node;
    }

    private ObjectNode headingNode(int level, String text) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("type", "heading");
        node.putObject("attrs").put("level", level);
        if (!text.isEmpty()) {
            node.putArray("content").add(textNode(text));
        }
        return node;
    }

    private ObjectNode bulletListNode(String block) {
        ObjectNode list = objectMapper.createObjectNode();
        list.put("type", "bulletList");
        ArrayNode items = list.putArray("content");

        for (String line : block.lines().toList()) {
            String itemText = line.strip().substring(2).strip();
            ObjectNode listItem = objectMapper.createObjectNode();
            listItem.put("type", "listItem");
            listItem.putArray("content").add(paragraphNode(itemText));
            items.add(listItem);
        }
        return list;
    }

    private ObjectNode textNode(String text) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("type", "text");
        node.put("text", text);
        return node;
    }
}
