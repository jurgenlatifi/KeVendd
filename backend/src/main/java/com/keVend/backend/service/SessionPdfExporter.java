package com.keVend.backend.service;

import com.keVend.backend.dto.OwnerSessionView;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * FR-13: renders an owner's session list to a PDF byte stream. NFR-14:
 * driver column carries only the anonymous ID — no name or phone.
 */
@Service
public class SessionPdfExporter {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public byte[] render(String ownerLabel, LocalDate from, LocalDate to,
                         List<OwnerSessionView> sessions) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font header = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

            doc.add(new Paragraph("KeVend — Owner Session Report", title));
            doc.add(new Paragraph(ownerLabel, normal));
            doc.add(new Paragraph(
                    "Range: " + (from != null ? from : "—") + "  to  " + (to != null ? to : "—"),
                    normal));
            doc.add(new Paragraph(" ", normal));

            PdfPTable table = new PdfPTable(new float[]{2.5f, 2.5f, 2.5f, 1.5f, 1.5f, 1.5f, 1.5f});
            table.setWidthPercentage(100);
            for (String h : List.of("Start", "End", "Lot", "Spots", "Status", "Total", "Owner Earn.")) {
                PdfPCell cell = new PdfPCell(new Paragraph(h, header));
                cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
                table.addCell(cell);
            }

            BigDecimal totalRevenue = BigDecimal.ZERO;
            BigDecimal totalEarnings = BigDecimal.ZERO;
            for (OwnerSessionView s : sessions) {
                table.addCell(cell(s.getStartTime() != null
                        ? s.getStartTime().atZone(java.time.ZoneId.systemDefault())
                            .toLocalDateTime().format(DATE_FMT)
                        : "—", normal));
                table.addCell(cell(s.getEndTime() != null
                        ? s.getEndTime().atZone(java.time.ZoneId.systemDefault())
                            .toLocalDateTime().format(DATE_FMT)
                        : "—", normal));
                table.addCell(cell(s.getParkingName() + "  (" + s.getDriverAnonymousId() + ")", normal));
                table.addCell(cell(String.valueOf(s.getSpotsReserved()), normal));
                table.addCell(cell(s.getStatus().name(), normal));
                table.addCell(cell(money(s.getTotalCost()), normal));
                table.addCell(cell(money(s.getOwnerRevenue()), normal));

                totalRevenue = totalRevenue.add(orZero(s.getTotalCost()));
                totalEarnings = totalEarnings.add(orZero(s.getOwnerRevenue()));
            }
            doc.add(table);

            doc.add(new Paragraph(" ", normal));
            doc.add(new Paragraph(
                    "Sessions: " + sessions.size() + "    Total: " + money(totalRevenue)
                            + "    Owner net: " + money(totalEarnings), header));

            doc.close();
            return out.toByteArray();
        } catch (DocumentException | java.io.IOException ex) {
            throw new IllegalStateException("PDF render failed", ex);
        }
    }

    private static PdfPCell cell(String text, Font font) {
        PdfPCell c = new PdfPCell(new Paragraph(text, font));
        c.setHorizontalAlignment(Element.ALIGN_LEFT);
        return c;
    }

    private static BigDecimal orZero(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    private static String money(BigDecimal v) {
        if (v == null) return "—";
        return v.toPlainString();
    }
}
