package com.docspace.service;

import com.docspace.entity.Document;
import com.docspace.entity.User;
import com.docspace.exception.ForbiddenException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

    @Mock
    private DocumentService documentService;

    private ExportService exportService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private User owner;
    private Document document;

    @BeforeEach
    void setUp() {
        exportService = new ExportService(documentService, objectMapper);

        owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setName("Shambhavi");
        owner.setEmail("shambhavi@ajaia.com");

        document = new Document();
        document.setId(UUID.randomUUID());
        document.setTitle("Export Test");
        document.setContent(
                "{\"type\":\"doc\",\"content\":[" +
                        "{\"type\":\"heading\",\"attrs\":{\"level\":1},\"content\":[{\"type\":\"text\",\"text\":\"Title\"}]}," +
                        "{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"Some text.\"}]}," +
                        "{\"type\":\"bulletList\",\"content\":[" +
                        "{\"type\":\"listItem\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"item one\"}]}]}" +
                        "]}" +
                        "]}"
        );
        document.setOwner(owner);
        document.setVersion(0);
    }

    @Test
    void exportsMarkdownWithHeadingParagraphAndBulletList() {
        DocumentAccess access = new DocumentAccess(document, true, null);
        when(documentService.resolveAccess(document.getId(), owner.getId())).thenReturn(access);

        ExportService.ExportedDocument result = exportService.toMarkdown(document.getId(), owner.getId());
        String markdown = new String(result.bytes(), StandardCharsets.UTF_8);

        assertThat(result.title()).isEqualTo("Export Test");
        assertThat(markdown).contains("# Title");
        assertThat(markdown).contains("Some text.");
        assertThat(markdown).contains("- item one");
    }

    @Test
    void exportsPdfBytesStartingWithPdfMagicNumber() {
        DocumentAccess access = new DocumentAccess(document, true, null);
        when(documentService.resolveAccess(document.getId(), owner.getId())).thenReturn(access);

        ExportService.ExportedDocument result = exportService.toPdf(document.getId(), owner.getId());

        assertThat(result.bytes().length).isGreaterThan(0);
        String header = new String(result.bytes(), 0, 4, StandardCharsets.US_ASCII);
        assertThat(header).isEqualTo("%PDF");
    }

    @Test
    void userWithoutAccessCannotExport() {
        UUID strangerId = UUID.randomUUID();
        DocumentAccess noAccess = new DocumentAccess(document, false, null);
        when(documentService.resolveAccess(document.getId(), strangerId)).thenReturn(noAccess);

        assertThatThrownBy(() -> exportService.toMarkdown(document.getId(), strangerId))
                .isInstanceOf(ForbiddenException.class);
    }
}
