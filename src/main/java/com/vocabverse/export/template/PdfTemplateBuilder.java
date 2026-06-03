package com.vocabverse.export.template;

import com.vocabverse.export.dto.ExportVocabularyRow;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PdfTemplateBuilder {

    private static final int PAGE_WIDTH = 595;
    private static final int PAGE_HEIGHT = 842;
    private static final int LEFT_MARGIN = 50;
    private static final int TOP_Y = 790;
    private static final int BOTTOM_Y = 60;
    private static final int LINE_HEIGHT = 14;
    private static final int MAX_LINE_CHARS = 88;

    public byte[] buildVocabularyPdf(String title, List<ExportVocabularyRow> rows) {
        List<List<String>> pages = paginate(buildLines(title, rows));
        return buildPdf(pages);
    }

    private List<String> buildLines(String title, List<ExportVocabularyRow> rows) {
        List<String> lines = new ArrayList<>();
        lines.add(nonBlank(title, "Vocabulary Export"));
        lines.add("Generated at: " + LocalDateTime.now());
        lines.add("");

        if (rows.isEmpty()) {
            lines.add("No vocabularies found.");
            return lines;
        }

        for (int index = 0; index < rows.size(); index++) {
            ExportVocabularyRow row = rows.get(index);
            lines.add((index + 1) + ". " + nonBlank(row.term(), "-"));
            lines.add("   Meaning: " + nonBlank(row.meaning(), "-"));
            lines.add("   Vietnamese meaning: " + nonBlank(row.vietnameseMeaning(), "-"));
            lines.add("   Pronunciation: " + nonBlank(row.pronunciation(), "-"));
            lines.add("   Part of speech: " + nonBlank(row.partOfSpeech(), "-"));
            lines.add("   Example: " + nonBlank(row.exampleSentence(), "-"));
            lines.add("   Note: " + nonBlank(row.note(), "-"));
            lines.add("");
        }

        return lines;
    }

    private List<List<String>> paginate(List<String> rawLines) {
        List<List<String>> pages = new ArrayList<>();
        List<String> currentPage = new ArrayList<>();
        int maxLinesPerPage = (TOP_Y - BOTTOM_Y) / LINE_HEIGHT;

        for (String rawLine : rawLines) {
            for (String line : wrap(rawLine)) {
                if (currentPage.size() >= maxLinesPerPage) {
                    pages.add(currentPage);
                    currentPage = new ArrayList<>();
                }
                currentPage.add(line);
            }
        }

        if (currentPage.isEmpty()) {
            currentPage.add("");
        }
        pages.add(currentPage);

        return pages;
    }

    private List<String> wrap(String line) {
        String sanitized = sanitize(line);
        if (sanitized.length() <= MAX_LINE_CHARS) {
            return List.of(sanitized);
        }

        List<String> lines = new ArrayList<>();
        String remaining = sanitized;
        while (remaining.length() > MAX_LINE_CHARS) {
            int splitAt = remaining.lastIndexOf(' ', MAX_LINE_CHARS);
            if (splitAt <= 0) {
                splitAt = MAX_LINE_CHARS;
            }
            lines.add(remaining.substring(0, splitAt).stripTrailing());
            remaining = remaining.substring(splitAt).stripLeading();
        }
        if (!remaining.isBlank()) {
            lines.add(remaining);
        }
        return lines;
    }

    private byte[] buildPdf(List<List<String>> pages) {
        int pageCount = pages.size();
        int fontObjectNumber = 3 + (pageCount * 2);
        int totalObjects = fontObjectNumber;
        List<String> objects = new ArrayList<>();

        objects.add("<< /Type /Catalog /Pages 2 0 R >>");
        objects.add(buildPagesObject(pageCount));

        int contentObjectNumber = 4;
        for (List<String> page : pages) {
            objects.add(buildPageObject(contentObjectNumber, fontObjectNumber));
            objects.add(buildContentObject(page));
            contentObjectNumber += 2;
        }

        objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(out, "%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();
        offsets.add(0);

        for (int i = 0; i < objects.size(); i++) {
            offsets.add(out.size());
            write(out, (i + 1) + " 0 obj\n");
            write(out, objects.get(i));
            write(out, "\nendobj\n");
        }

        int xrefOffset = out.size();
        write(out, "xref\n");
        write(out, "0 " + (totalObjects + 1) + "\n");
        write(out, "0000000000 65535 f \n");
        for (int i = 1; i < offsets.size(); i++) {
            write(out, String.format("%010d 00000 n \n", offsets.get(i)));
        }
        write(out, "trailer\n");
        write(out, "<< /Size " + (totalObjects + 1) + " /Root 1 0 R >>\n");
        write(out, "startxref\n");
        write(out, xrefOffset + "\n");
        write(out, "%%EOF");

        return out.toByteArray();
    }

    private String buildPagesObject(int pageCount) {
        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < pageCount; i++) {
            kids.append(3 + (i * 2)).append(" 0 R ");
        }
        return "<< /Type /Pages /Kids [" + kids + "] /Count " + pageCount + " >>";
    }

    private String buildPageObject(int contentObjectNumber, int fontObjectNumber) {
        return "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + PAGE_WIDTH + " " + PAGE_HEIGHT + "] "
                + "/Resources << /Font << /F1 " + fontObjectNumber + " 0 R >> >> "
                + "/Contents " + contentObjectNumber + " 0 R >>";
    }

    private String buildContentObject(List<String> lines) {
        StringBuilder stream = new StringBuilder();
        stream.append("BT\n");
        stream.append("/F1 10 Tf\n");
        stream.append(LEFT_MARGIN).append(" ").append(TOP_Y).append(" Td\n");
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                stream.append("0 -").append(LINE_HEIGHT).append(" Td\n");
            }
            stream.append("(").append(escapePdfString(lines.get(i))).append(") Tj\n");
        }
        stream.append("ET\n");

        byte[] bytes = stream.toString().getBytes(StandardCharsets.ISO_8859_1);
        return "<< /Length " + bytes.length + " >>\nstream\n" + stream + "endstream";
    }

    private String escapePdfString(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }

    private String sanitize(String text) {
        return nonBlank(text, "")
                .replaceAll("[\\r\\n\\t]+", " ")
                .replaceAll("[^\\x20-\\x7E]", "?")
                .strip();
    }

    private String nonBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private void write(ByteArrayOutputStream out, String value) {
        out.writeBytes(value.getBytes(StandardCharsets.ISO_8859_1));
    }
}
