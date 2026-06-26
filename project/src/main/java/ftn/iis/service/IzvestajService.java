package ftn.iis.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import ftn.iis.model.Knjiga;
import ftn.iis.model.Pozajmica;
import ftn.iis.repository.PozajmicaRepository;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class IzvestajService {
    private final PozajmicaRepository pozajmicaRepository;

    private static final Color BROWN_DARK   = new Color(94,  68,  54);
    private static final Color BROWN_MED    = new Color(122, 92,  72);
    private static final Color BROWN_LIGHT  = new Color(168, 144, 128);
    private static final Color BEIGE_LIGHT  = new Color(250, 248, 246);
    private static final Color BEIGE_MED    = new Color(240, 232, 224);
    private static final Color TEXT_DARK    = new Color(40,  35,  30);
    private static final Color TEXT_MID     = new Color(100, 90,  80);
    private static final Color WHITE        = Color.WHITE;
    private static final Color GREEN_LIGHT  = new Color(212, 221, 184);
    private static final Color GREEN_DARK   = new Color(74,  103,  65);

    private static final Color GREEN_MED   = new Color(122, 144, 104);
    private static final Color CARD_BG     = new Color(200, 213, 170);
    private static final Color CARD_LIGHT  = new Color(234, 239, 220);
    private static final Color TEXT_DARK_GREEN   = new Color(30, 45, 20);
    private static final Color TEXT_MID_GREEN   = new Color(58, 78, 48);

    private static final String[] MESECI = {
            "", "Januar", "Februar", "Mart", "April", "Maj", "Jun",
            "Jul", "Avgust", "Septembar", "Oktobar", "Novembar", "Decembar"
    };

    public IzvestajService(PozajmicaRepository pozajmicaRepository) {
        this.pozajmicaRepository = pozajmicaRepository;
    }

    public byte[] generisiIzvestaj(LocalDate od, LocalDate datDo) throws DocumentException, IOException {
        java.util.List<Pozajmica> pozajmice = pozajmicaRepository.findAllInPeriodWithDetails(od, datDo);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 45, 45, 55, 45);
        PdfWriter writer = PdfWriter.getInstance(doc, baos);

        writer.setPageEvent(new PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter w, Document d) {
                try {
                    BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, "Cp1250", BaseFont.NOT_EMBEDDED);
                    Font footer = new Font(bf, 8, Font.NORMAL, TEXT_MID_GREEN);
                    PdfContentByte cb = w.getDirectContent();
                    cb.saveState();
                    cb.setColorFill(CARD_BG);
                    cb.rectangle(d.left(), d.bottom() - 15, d.right() - d.left(), 1);
                    cb.fill();
                    ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                            new Phrase("Strana " + w.getPageNumber(), footer),
                            (d.left() + d.right()) / 2, d.bottom() - 25, 0);
                    cb.restoreState();
                } catch (Exception ignored) {}
            }
        });

        doc.open();

        BaseFont bf     = BaseFont.createFont(BaseFont.HELVETICA,      "Cp1250", BaseFont.NOT_EMBEDDED);
        BaseFont bfBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, "Cp1250", BaseFont.NOT_EMBEDDED);

        Font fTitle    = new Font(bfBold, 20, Font.BOLD,   WHITE);
        Font fSubtitle = new Font(bf,     10, Font.NORMAL, GREEN_LIGHT);
        Font fSection  = new Font(bfBold, 13, Font.BOLD,   GREEN_DARK);
        Font fTblHead  = new Font(bfBold,  9, Font.BOLD,   WHITE);
        Font fTblCell  = new Font(bf,      9, Font.NORMAL, TEXT_DARK_GREEN);
        Font fTblSmall = new Font(bf,      8, Font.NORMAL, TEXT_MID_GREEN);
        Font fMetVal   = new Font(bfBold, 15, Font.BOLD,   GREEN_MED);
        Font fMetLbl   = new Font(bf,      9, Font.NORMAL, TEXT_MID_GREEN);
        Font fNormal   = new Font(bf,      10, Font.NORMAL, TEXT_DARK_GREEN);



        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        header.setSpacingAfter(20);

        PdfPCell hCell = new PdfPCell();
        hCell.setBackgroundColor(GREEN_DARK);
        hCell.setPadding(22);
        hCell.setBorder(Rectangle.NO_BORDER);

        Paragraph t1 = new Paragraph("Izvestaj o aktivnostima biblioteke", fTitle);
        t1.setAlignment(Element.ALIGN_CENTER);
        Paragraph t2 = new Paragraph(
                "Period: " + fmt(od) + "  \u2014  " + fmt(datDo) +
                        "    |    Generisano: " + fmt(LocalDate.now()), fSubtitle);
        t2.setAlignment(Element.ALIGN_CENTER);
        t2.setSpacingBefore(5);

        hCell.addElement(t1);
        hCell.addElement(t2);
        header.addCell(hCell);
        doc.add(header);


        int total = pozajmice.size();
        LocalDate danas = LocalDate.now();

        // Aktivni clanovi
        long aktivnihClanova = pozajmice.stream()
                .map(p -> p.getClan().getJmbg()).distinct().count();

        // Prosecno trajanje (samo vracene)
        OptionalDouble prosecnoTrajanje = pozajmice.stream()
                .filter(p -> p.getDatVrac() != null)
                .mapToLong(p -> ChronoUnit.DAYS.between(p.getDatPoz(), p.getDatVrac()))
                .average();

        // Stopa prekoracenja
        long prekoraciloRok = pozajmice.stream()
                .filter(p -> p.getDatVrac() != null && p.getDatVrac().isAfter(p.getDatOcVrac()))
                .count();
        long josUPrekoracenju = pozajmice.stream()
                .filter(p -> p.getDatVrac() == null && danas.isAfter(p.getDatOcVrac()))
                .count();
        long ukupnoPrekoracilo = prekoraciloRok + josUPrekoracenju;

        // Top clanovi
        Map<String, Long> countByJmbg = pozajmice.stream()
                .collect(Collectors.groupingBy(p -> p.getClan().getJmbg(), Collectors.counting()));
        List<Map.Entry<String, Long>> topClanovi = countByJmbg.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5).collect(Collectors.toList());
        Map<String, String> jmbgToName = pozajmice.stream()
                .collect(Collectors.toMap(p -> p.getClan().getJmbg(),
                        p -> p.getClan().getFirstName() + " " + p.getClan().getLastName(),
                        (a, b) -> a));

        // Po kategoriji clana
        Map<String, Long> poKategoriji = pozajmice.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getClan().getKategorijaClana() != null
                                ? p.getClan().getKategorijaClana().getTipKC().name()
                                : "Bez kategorije",
                        Collectors.counting()));

        // Top knjige (by isbn)
        Map<String, Long> countByIsbn = pozajmice.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getPrimerakKnjige().getFizickaKnjiga().getIsbn(), Collectors.counting()));
        Map<String, Knjiga> isbnToKnjiga = pozajmice.stream()
                .collect(Collectors.toMap(
                        p -> p.getPrimerakKnjige().getFizickaKnjiga().getIsbn(),
                        p -> p.getPrimerakKnjige().getFizickaKnjiga().getKnjiga(),
                        (a, b) -> a));
        List<Map.Entry<String, Long>> topKnjige = countByIsbn.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10).collect(Collectors.toList());

        // Po zanru
        Map<String, Long> poZanru = pozajmice.stream()
                .collect(Collectors.groupingBy(
                        p -> {
                            var zanr = p.getPrimerakKnjige().getFizickaKnjiga().getKnjiga().getZanr();
                            return zanr != null ? zanr.getName() : "Bez zanra";
                        }, Collectors.counting()));

        // Mesecni trend
        Map<YearMonth, Long> mesecniTrend = new TreeMap<>(pozajmice.stream()
                .collect(Collectors.groupingBy(
                        p -> YearMonth.of(p.getDatPoz().getYear(), p.getDatPoz().getMonthValue()),
                        Collectors.counting())));

        doc.add(sectionHeader("1. Aktivnost clanova", fSection, bfBold));

        // Karticice sa sumama
        PdfPTable metrics1 = new PdfPTable(3);
        metrics1.setWidthPercentage(100);
        metrics1.setSpacingBefore(10);
        metrics1.setSpacingAfter(16);
        metrics1.setWidths(new float[]{1, 1, 1});

        metrics1.addCell(metricCell("" + total,          "Ukupno pozajmica",      fMetVal, fMetLbl, bfBold));
        metrics1.addCell(metricCell("" + aktivnihClanova,"Aktivnih clanova",       fMetVal, fMetLbl, bfBold));
        metrics1.addCell(metricCell(
                prosecnoTrajanje.isPresent()
                        ? String.format("%.1f dan", prosecnoTrajanje.getAsDouble()) : "—",
                "Prosecno trajanje poz.", fMetVal, fMetLbl, bfBold));
        doc.add(metrics1);

        // Top 5 clanova
        doc.add(subheading("Top 5 najaktivnijih clanova", new Font(bfBold, 10, Font.BOLD, TEXT_MID_GREEN)));
        PdfPTable tblClanovi = new PdfPTable(3);
        tblClanovi.setWidthPercentage(100);
        tblClanovi.setSpacingBefore(6);
        tblClanovi.setSpacingAfter(14);
        tblClanovi.setWidths(new float[]{0.5f, 3f, 1.5f});

        addTableHeader(tblClanovi, fTblHead, GREEN_DARK, "#", "Ime i prezime", "Br. pozajmica");
        int rank = 1;
        for (Map.Entry<String, Long> e : topClanovi) {
            Color bg = (rank % 2 == 0) ? CARD_LIGHT : WHITE;
            addRow(tblClanovi, fTblCell, bg, Element.ALIGN_CENTER, "" + rank,
                    jmbgToName.getOrDefault(e.getKey(), e.getKey()),
                    "" + e.getValue());
            rank++;
        }
        if (topClanovi.isEmpty()) addEmptyRow(tblClanovi, fTblSmall, 3, "Nema podataka za izabrani period");
        doc.add(tblClanovi);

        // Po kategoriji
        doc.add(subheading("Pozajmice po kategoriji clana", new Font(bfBold, 10, Font.BOLD, TEXT_MID_GREEN)));
        PdfPTable tblKat = new PdfPTable(3);
        tblKat.setWidthPercentage(70);
        tblKat.setHorizontalAlignment(Element.ALIGN_LEFT);
        tblKat.setSpacingBefore(6);
        tblKat.setSpacingAfter(20);
        tblKat.setWidths(new float[]{3f, 1.5f, 1f});

        addTableHeader(tblKat, fTblHead, GREEN_DARK, "Kategorija", "Br. pozajmica", "%");
        int rowIdx = 1;
        for (Map.Entry<String, Long> e : sortedDesc(poKategoriji)) {
            Color bg = (rowIdx % 2 == 0) ? CARD_LIGHT : WHITE;
            String pct = total > 0 ? String.format("%.1f%%", 100.0 * e.getValue() / total) : "—";
            addRow(tblKat, fTblCell, bg, Element.ALIGN_LEFT, labelKategorije(e.getKey()), "" + e.getValue(), pct);
            rowIdx++;
        }
        if (poKategoriji.isEmpty()) addEmptyRow(tblKat, fTblSmall, 3, "Nema podataka");
        doc.add(tblKat);


        doc.add(sectionHeader("2. Popularnost naslova", fSection, bfBold));

        doc.add(subheading("Top 10 najpozajmljivanijih knjiga", new Font(bfBold, 10, Font.BOLD, TEXT_MID_GREEN)));
        PdfPTable tblKnjige = new PdfPTable(5);
        tblKnjige.setWidthPercentage(100);
        tblKnjige.setSpacingBefore(6);
        tblKnjige.setSpacingAfter(14);
        tblKnjige.setWidths(new float[]{0.5f, 3.5f, 2f, 1.5f, 1f});

        addTableHeader(tblKnjige, fTblHead, GREEN_DARK, "#", "Naslov", "Autor", "Zanr", "Br.");
        int rk = 1;
        for (Map.Entry<String, Long> e : topKnjige) {
            Color bg = (rk % 2 == 0) ? CARD_LIGHT : WHITE;
            Knjiga k = isbnToKnjiga.get(e.getKey());
            String naslov = truncate(k.getNaslov(), 40);
            String autor  = truncate(k.getAutor() != null ? k.getAutor() : "—", 25);
            String zanr   = k.getZanr() != null ? k.getZanr().getName() : "—";
            addRow(tblKnjige, fTblCell, bg, Element.ALIGN_CENTER, "" + rk, naslov, autor, zanr, "" + e.getValue());
            rk++;
        }
        if (topKnjige.isEmpty()) addEmptyRow(tblKnjige, fTblSmall, 5, "Nema podataka za izabrani period");
        doc.add(tblKnjige);

        // Po zanru
        doc.add(subheading("Pozajmice po zanru", new Font(bfBold, 10, Font.BOLD, TEXT_MID_GREEN)));
        PdfPTable tblZanr = new PdfPTable(3);
        tblZanr.setWidthPercentage(65);
        tblZanr.setHorizontalAlignment(Element.ALIGN_LEFT);
        tblZanr.setSpacingBefore(6);
        tblZanr.setSpacingAfter(20);
        tblZanr.setWidths(new float[]{3f, 1.5f, 1f});

        addTableHeader(tblZanr, fTblHead, GREEN_DARK, "Zanr", "Br. pozajmica", "%");
        rowIdx = 1;
        for (Map.Entry<String, Long> e : sortedDesc(poZanru)) {
            Color bg = (rowIdx % 2 == 0) ? CARD_LIGHT : WHITE;
            String pct = total > 0 ? String.format("%.1f%%", 100.0 * e.getValue() / total) : "—";
            addRow(tblZanr, fTblCell, bg, Element.ALIGN_LEFT, e.getKey(), "" + e.getValue(), pct);
            rowIdx++;
        }
        if (poZanru.isEmpty()) addEmptyRow(tblZanr, fTblSmall, 3, "Nema podataka");
        doc.add(tblZanr);


        doc.add(sectionHeader("3. Trendovi citanja", fSection, bfBold));

        // Sumarni metrik — prekoracenja
        PdfPTable metrics3 = new PdfPTable(2);
        metrics3.setWidthPercentage(60);
        metrics3.setHorizontalAlignment(Element.ALIGN_LEFT);
        metrics3.setSpacingBefore(10);
        metrics3.setSpacingAfter(16);

        String stopaStr = total > 0
                ? String.format("%.1f%%", 100.0 * ukupnoPrekoracilo / total)
                : "—";
        metrics3.addCell(metricCell(stopaStr, "Stopa prekoracenja roka", fMetVal, fMetLbl, bfBold));
        metrics3.addCell(metricCell("" + ukupnoPrekoracilo, "Ukupno prekoraceno", fMetVal, fMetLbl, bfBold));
        doc.add(metrics3);

        // Mesecni pregled
        doc.add(subheading("Mesecni pregled pozajmica", new Font(bfBold, 10, Font.BOLD, TEXT_MID_GREEN)));
        PdfPTable tblMes = new PdfPTable(3);
        tblMes.setWidthPercentage(65);
        tblMes.setHorizontalAlignment(Element.ALIGN_LEFT);
        tblMes.setSpacingBefore(6);
        tblMes.setSpacingAfter(14);
        tblMes.setWidths(new float[]{2.5f, 1.5f, 1.5f});

        addTableHeader(tblMes, fTblHead, GREEN_DARK, "Mesec", "Br. pozajmica", "Promena");
        rowIdx = 1;
        Long prevCount = null;
        for (Map.Entry<YearMonth, Long> e : mesecniTrend.entrySet()) {
            Color bg = (rowIdx % 2 == 0) ? CARD_LIGHT : WHITE;
            String label = MESECI[e.getKey().getMonthValue()] + " " + e.getKey().getYear();
            String promena;
            if (prevCount == null) {
                promena = "—";
            } else {
                long diff = e.getValue() - prevCount;
                promena = (diff >= 0 ? "+" : "") + diff;
            }
            addRow(tblMes, fTblCell, bg, Element.ALIGN_LEFT, label, "" + e.getValue(), promena);
            prevCount = e.getValue();
            rowIdx++;
        }
        if (mesecniTrend.isEmpty()) addEmptyRow(tblMes, fTblSmall, 3, "Nema podataka za izabrani period");
        doc.add(tblMes);


        doc.close();
        return baos.toByteArray();
    }


    private Paragraph sectionHeader(String tekst, Font fSection, BaseFont bfBold) throws DocumentException {
        Paragraph p = new Paragraph();
        p.setSpacingBefore(14);
        p.setSpacingAfter(2);

        Chunk c = new Chunk(tekst, fSection);
        p.add(c);
        return p;
    }

    private Paragraph subheading(String tekst, Font font) {
        Paragraph p = new Paragraph(tekst, font);
        p.setSpacingBefore(6);
        p.setSpacingAfter(2);
        return p;
    }

    private PdfPCell metricCell(String value, String label, Font fVal, Font fLbl, BaseFont bfBold) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(CARD_BG);
        cell.setPadding(14);
        cell.setBorderColor(GREEN_LIGHT);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph val = new Paragraph(value, fVal);
        val.setAlignment(Element.ALIGN_CENTER);
        Paragraph lbl = new Paragraph(label, fLbl);
        lbl.setAlignment(Element.ALIGN_CENTER);

        cell.addElement(val);
        cell.addElement(lbl);
        return cell;
    }

    private void addTableHeader(PdfPTable table, Font font, Color bg, String... headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, font));
            cell.setBackgroundColor(bg);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPaddingTop(7);
            cell.setPaddingBottom(7);
            cell.setPaddingLeft(6);
            cell.setPaddingRight(6);
            cell.setBorderColor(GREEN_DARK);
            table.addCell(cell);
        }
    }

    private void addRow(PdfPTable table, Font font, Color bg, int firstAlign, String... values) {
        boolean first = true;
        for (String v : values) {
            PdfPCell cell = new PdfPCell(new Phrase(v != null ? v : "—", font));
            cell.setBackgroundColor(bg);
            cell.setHorizontalAlignment(first ? firstAlign : Element.ALIGN_CENTER);
            cell.setPaddingTop(5);
            cell.setPaddingBottom(5);
            cell.setPaddingLeft(6);
            cell.setPaddingRight(6);
            cell.setBorderColor(GREEN_LIGHT);
            table.addCell(cell);
            first = false;
        }
    }

    private void addEmptyRow(PdfPTable table, Font font, int colspan, String message) {
        PdfPCell cell = new PdfPCell(new Phrase(message, font));
        cell.setColspan(colspan);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(10);
        cell.setBorderColor(GREEN_LIGHT);
        table.addCell(cell);
    }

    private List<Map.Entry<String, Long>> sortedDesc(Map<String, Long> map) {
        return map.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toList());
    }

    private String fmt(LocalDate d) {
        return d != null ? d.format(DateTimeFormatter.ofPattern("dd.MM.yyyy.")) : "—";
    }

    private String truncate(String s, int max) {
        if (s == null) return "—";
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }

    private String labelKategorije(String tip) {
        return switch (tip) {
            case "REGULARNA"     -> "Regularna";
            case "DECIJA"        -> "Decija";
            case "STUDENTSKA"    -> "Studentska";
            case "PENZIONERSKA"  -> "Penzionerska";
            case "PORODICNA"     -> "Porodicna";
            default              -> tip;
        };
    }
}
