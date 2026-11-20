package com.docspace.service;

import com.docspace.dto.DocumentResponse;
import com.docspace.exception.BadRequestException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileImportServiceTest {

    @Mock
    private DocumentService documentService;

    private FileImportService fileImportService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UUID ownerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        fileImportService = new FileImportService(documentService, objectMapper);
    }

    // 7. Unsupported file upload is rejected
    @Test
    void rejectsUnsupportedExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "notes.pdf", "application/pdf", "hello".getBytes(StandardCharsets.UTF_8)
        );

        assertThatThrownBy(() -> fileImportService.importFile(ownerId, file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Only .txt, .md, and .docx");
    }

    @Test
    void rejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "notes.txt", "text/plain", new byte[0]
        );

        assertThatThrownBy(() -> fileImportService.importFile(ownerId, file))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsFileOverTwoMegabytes() {
        byte[] bigContent = new byte[2 * 1024 * 1024 + 1];
        MockMultipartFile file = new MockMultipartFile(
                "file", "big.txt", "text/plain", bigContent
        );

        assertThatThrownBy(() -> fileImportService.importFile(ownerId, file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("2MB");
    }

    @Test
    void importsPlainTextAsDocumentWithTitleFromFilename() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "My Notes.txt", "text/plain", "Hello world".getBytes(StandardCharsets.UTF_8)
        );

        when(documentService.createFromImport(eq(ownerId), eq("My Notes"), any(JsonNode.class)))
                .thenAnswer(invocation -> new DocumentResponse(
                        UUID.randomUUID(), "My Notes", invocation.getArgument(2), null, 0, "OWNER", null, null
                ));

        DocumentResponse response = fileImportService.importFile(ownerId, file);

        assertThat(response.title()).isEqualTo("My Notes");
    }

    @Test
    void importsMarkdownHeadingsIntoStructuredContent() {
        String markdown = "# Title\n\nSome intro.\n\n## Section\n\n- one\n- two";
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.md", "text/markdown", markdown.getBytes(StandardCharsets.UTF_8)
        );

        when(documentService.createFromImport(eq(ownerId), eq("doc"), any(JsonNode.class)))
                .thenAnswer(invocation -> {
                    JsonNode content = invocation.getArgument(2);
                    return new DocumentResponse(UUID.randomUUID(), "doc", content, null, 0, "OWNER", null, null);
                });

        DocumentResponse response = fileImportService.importFile(ownerId, file);

        JsonNode firstBlock = response.content().get("content").get(0);
        assertThat(firstBlock.get("type").asText()).isEqualTo("heading");
    }

    @Test
    void parsesDocxHeadingsAndParagraphsIntoStructuredContent() throws Exception {
        byte[] docxBytes = buildSampleDocx();

        JsonNode content = fileImportService.parseDocxStream(new ByteArrayInputStream(docxBytes));

        JsonNode blocks = content.get("content");
        assertThat(blocks).hasSize(2);
        assertThat(blocks.get(0).get("type").asText()).isEqualTo("heading");
        assertThat(blocks.get(0).get("attrs").get("level").asInt()).isEqualTo(1);
        assertThat(blocks.get(0).get("content").get(0).get("text").asText()).isEqualTo("Document Title");
        assertThat(blocks.get(1).get("type").asText()).isEqualTo("paragraph");
        assertThat(blocks.get(1).get("content").get(0).get("text").asText())
                .isEqualTo("This is a plain paragraph imported from a real .docx file.");
    }

    @Test
    void importFileRoutesDocxExtensionThroughDocxParser() throws Exception {
        byte[] docxBytes = buildSampleDocx();
        MockMultipartFile file = new MockMultipartFile(
                "file", "Report.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                docxBytes
        );

        when(documentService.createFromImport(eq(ownerId), eq("Report"), any(JsonNode.class)))
                .thenAnswer(invocation -> new DocumentResponse(
                        UUID.randomUUID(), "Report", invocation.getArgument(2), null, 0, "OWNER", null, null
                ));

        DocumentResponse response = fileImportService.importFile(ownerId, file);

        assertThat(response.title()).isEqualTo("Report");
        assertThat(response.content().get("content").get(0).get("type").asText()).isEqualTo("heading");
    }

    private byte[] buildSampleDocx() throws Exception {
        try (XWPFDocument document = new XWPFDocument()) {
            XWPFParagraph heading = document.createParagraph();
            heading.setStyle("Heading1");
            XWPFRun headingRun = heading.createRun();
            headingRun.setText("Document Title");

            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun paragraphRun = paragraph.createRun();
            paragraphRun.setText("This is a plain paragraph imported from a real .docx file.");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.write(out);
            return out.toByteArray();
        }
    }
}
