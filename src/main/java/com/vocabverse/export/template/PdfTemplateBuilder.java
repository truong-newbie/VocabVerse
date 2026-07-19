package com.vocabverse.export.template;

import com.vocabverse.export.dto.ExportVocabularyRow;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class PdfTemplateBuilder {

    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float LEFT_MARGIN = 50;
    private static final float RIGHT_MARGIN = PAGE_WIDTH - 50;
    private static final float TOP_MARGIN = PAGE_HEIGHT - 50;
    private static final float BOTTOM_MARGIN = 60;
    private static final float LINE_HEIGHT = 16;
    private static final float TITLE_SIZE = 18;
    private static final float HEADING_SIZE = 12;
    private static final float BODY_SIZE = 11;

    public byte[] buildVocabularyPdf(String title, List<ExportVocabularyRow> rows) {
        try (PDDocument document = new PDDocument()) {
            PDFont regularFont = loadFont(document, "fonts/DejaVuSans.ttf");
            PDFont boldFont = loadFont(document, "fonts/DejaVuSans-Bold.ttf");
            List<ContentBlock> blocks = buildBlocks(title, rows);
            renderBlocks(document, blocks, regularFont, boldFont);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private PDFont loadFont(PDDocument document, String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream is = resource.getInputStream()) {
            return PDType0Font.load(document, is, true);
        }
    }

    private List<ContentBlock> buildBlocks(String title, List<ExportVocabularyRow> rows) {
        List<ContentBlock> blocks = new ArrayList<>();
        blocks.add(new ContentBlock(BlockType.TITLE, title.isBlank() ? "Vocabulary Export" : title));
        blocks.add(new ContentBlock(BlockType.SUBTITLE, "Generated at: " + LocalDateTime.now()));
        blocks.add(new ContentBlock(BlockType.SPACE, ""));

        if (rows.isEmpty()) {
            blocks.add(new ContentBlock(BlockType.BODY, "No vocabularies found."));
            return blocks;
        }

        for (int i = 0; i < rows.size(); i++) {
            ExportVocabularyRow row = rows.get(i);
            blocks.add(new ContentBlock(BlockType.WORD_NUMBER, (i + 1) + ". " + nonBlank(row.term(), "-")));
            blocks.add(new ContentBlock(BlockType.WORD_VALUE, nonBlank(row.term(), "-")));
            blocks.add(new ContentBlock(BlockType.LABEL_VALUE, "Pronunciation: " + nonBlank(row.pronunciation(), "-")));
            blocks.add(new ContentBlock(BlockType.LABEL_VALUE, "Part of speech: " + nonBlank(row.partOfSpeech(), "-")));
            blocks.add(new ContentBlock(BlockType.LABEL_VALUE, "Meaning: " + nonBlank(row.meaning(), "-")));
            blocks.add(new ContentBlock(BlockType.LABEL_VALUE, "Vietnamese meaning: " + nonBlank(row.vietnameseMeaning(), "-")));
            blocks.add(new ContentBlock(BlockType.LABEL_VALUE, "Example: " + nonBlank(row.exampleSentence(), "-")));
            blocks.add(new ContentBlock(BlockType.SPACE, ""));
        }

        return blocks;
    }

    private void renderBlocks(PDDocument document, List<ContentBlock> blocks,
                              PDFont regularFont, PDFont boldFont) throws IOException {
        PDPage currentPage = new PDPage(PDRectangle.A4);
        document.addPage(currentPage);

        float y = TOP_MARGIN;
        PDPageContentStream content = new PDPageContentStream(document, currentPage);
        content.setFont(regularFont, BODY_SIZE);

        for (ContentBlock block : blocks) {
            float fontSize = block.type.fontSize;
            content.setFont(block.type.useBold ? boldFont : regularFont, fontSize);
            float neededHeight = measureTextHeight(block.text, block.type.maxWidth(LEFT_MARGIN, RIGHT_MARGIN), fontSize);

            if (y - neededHeight < BOTTOM_MARGIN) {
                content.close();
                currentPage = new PDPage(PDRectangle.A4);
                document.addPage(currentPage);
                content = new PDPageContentStream(document, currentPage);
                content.setFont(regularFont, BODY_SIZE);
                y = TOP_MARGIN;
            }

            y = renderBlock(content, block, y, fontSize);
        }

        content.close();
    }

    private float renderBlock(PDPageContentStream content, ContentBlock block, float y,
                              float fontSize) throws IOException {
        if (block.type == BlockType.SPACE) {
            return y - LINE_HEIGHT * 0.5f;
        }

        float maxWidth = block.type.maxWidth(LEFT_MARGIN, RIGHT_MARGIN);

        if (block.type == BlockType.TITLE) {
            content.beginText();
            content.newLineAtOffset(LEFT_MARGIN, y);
            content.showText(block.text);
            content.endText();
            return y - LINE_HEIGHT * 1.5f;
        }

        if (block.type == BlockType.SUBTITLE) {
            content.beginText();
            content.newLineAtOffset(LEFT_MARGIN, y);
            content.showText(block.text);
            content.endText();
            return y - LINE_HEIGHT;
        }

        if (block.type == BlockType.WORD_NUMBER) {
            content.beginText();
            content.newLineAtOffset(LEFT_MARGIN, y);
            content.showText(block.text);
            content.endText();
            return y - LINE_HEIGHT;
        }

        if (block.type == BlockType.LABEL_VALUE) {
            List<String> lines = wrapText(block.text, maxWidth, fontSize);
            for (String line : lines) {
                if (y - LINE_HEIGHT < BOTTOM_MARGIN) {
                    break;
                }
                content.beginText();
                content.newLineAtOffset(LEFT_MARGIN + 15, y);
                content.showText(line);
                content.endText();
                y -= LINE_HEIGHT;
            }
            return y;
        }

        List<String> lines = wrapText(block.text, maxWidth, fontSize);
        for (String line : lines) {
            if (y - LINE_HEIGHT < BOTTOM_MARGIN) {
                break;
            }
            content.beginText();
            content.newLineAtOffset(LEFT_MARGIN + 15, y);
            content.showText(line);
            content.endText();
            y -= LINE_HEIGHT;
        }
        return y;
    }

    private List<String> wrapText(String text, float maxWidth, float fontSize) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            String testLine = line.length() == 0 ? word : line + " " + word;
            if (testLine.length() <= 88) {
                line = new StringBuilder(testLine);
            } else {
                if (line.length() > 0) {
                    result.add(line.toString());
                }
                line = new StringBuilder(word);
            }
        }
        if (line.length() > 0) {
            result.add(line.toString());
        }
        return result;
    }

    private float measureTextHeight(String text, float maxWidth, float fontSize) {
        List<String> lines = wrapText(text, maxWidth, fontSize);
        return lines.size() * LINE_HEIGHT;
    }

    private String nonBlank(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private enum BlockType {
        TITLE(18, true),
        SUBTITLE(12, false),
        WORD_NUMBER(12, true),
        WORD_VALUE(12, false),
        LABEL_VALUE(11, false),
        BODY(11, false),
        SPACE(0, false);

        private final float fontSize;
        private final boolean useBold;

        BlockType(float fontSize, boolean useBold) {
            this.fontSize = fontSize;
            this.useBold = useBold;
        }

        float maxWidth(float left, float right) {
            return right - left - (this == LABEL_VALUE || this == WORD_VALUE ? 15 : 0);
        }
    }

    private record ContentBlock(BlockType type, String text) {}
}
