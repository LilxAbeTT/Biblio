package com.biblio.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public class ReportService {

    public Path exportCSV(String name, List<String[]> rows) throws IOException {
        Path out = Path.of(name);
        try (Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out.toFile()), "UTF-8"))) {
            CSVPrinter printer = new CSVPrinter(w, CSVFormat.DEFAULT);
            for (String[] r : rows) printer.printRecord((Object[]) r);
            printer.flush();
        }
        return out;
    }

    public Path exportExcel(String name, List<String[]> rows) throws IOException {
        Path out = Path.of(name);
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet("Reporte");
            int r = 0;
            for (String[] row : rows) {
                Row rr = sh.createRow(r++);
                for (int c = 0; c < row.length; c++) {
                    rr.createCell(c).setCellValue(row[c] == null ? "" : row[c]);
                }
            }
            try (FileOutputStream fos = new FileOutputStream(out.toFile())) {
                wb.write(fos);
            }
        }
        return out;
    }

    public Path exportPDF(String name, String titulo, List<String[]> rows) throws IOException {
        Path out = Path.of(name);
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);

            PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font bodyFont  = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.setFont(titleFont, 14);
                cs.beginText();
                cs.newLineAtOffset(50, 750);
                cs.showText(titulo + " - " + LocalDate.now());
                cs.endText();

                float y = 720;
                cs.setFont(bodyFont, 10);
                for (String[] row : rows) {
                    float x = 50;
                    y -= 14;
                    for (String col : row) {
                        cs.beginText();
                        cs.newLineAtOffset(x, y);
                        cs.showText(col == null ? "" : col);
                        cs.endText();
                        x += 150;
                    }
                    if (y < 60) break; // paginación simple (mejorable)
                }
            }
            doc.save(out.toFile());
        }
        return out;
    }
}
