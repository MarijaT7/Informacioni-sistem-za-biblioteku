package ftn.iis.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import ftn.iis.model.Kazna;
import ftn.iis.model.Knjiga;
import ftn.iis.model.Pozajmica;
import ftn.iis.repository.KaznaRepository;
import ftn.iis.repository.KnjigaRepository;
import ftn.iis.repository.PozajmicaRepository;
import ftn.iis.repository.PrimerakKnjigeRepository;
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
    private final PozajmicaRepository     pozajmicaRepository;
    private final KnjigaRepository        knjigaRepository;
    private final PrimerakKnjigeRepository primerakKnjigeRepository;
    private final KaznaRepository         kaznaRepository;

    private static final Color BROWN_DARK   = new Color(94,  68,  54);
    private static final Color BROWN_MED    = new Color(122, 92,  72);
    private static final Color BROWN_LIGHT  = new Color(168, 144, 128);
    private static final Color BEIGE_LIGHT  = new Color(250, 248, 246);
    private static final Color BEIGE_MED    = new Color(240, 232, 224);
    private static final Color TEXT_DARK    = new Color(40,  35,  30);
    private static final Color TEXT_MID     = new Color(100, 90,  80);
    private static final Color WHITE        = Color.WHITE;
    private static final Color GREEN_LIGHT  = new Color(200, 240, 200);
    private static final Color GREEN_DARK   = new Color(29,  90,  38);
    private static final Color AMBER_DARK   = new Color(160, 100,   0);

    private static final String[] MESECI = {
            "", "Januar", "Februar", "Mart", "April", "Maj", "Jun",
            "Jul", "Avgust", "Septembar", "Oktobar", "Novembar", "Decembar"
    };

    public IzvestajService(PozajmicaRepository pozajmicaRepository,
                           KnjigaRepository knjigaRepository,
                           PrimerakKnjigeRepository primerakKnjigeRepository,
                           KaznaRepository kaznaRepository) {
        this.pozajmicaRepository      = pozajmicaRepository;
        this.knjigaRepository         = knjigaRepository;
        this.primerakKnjigeRepository = primerakKnjigeRepository;
        this.kaznaRepository          = kaznaRepository;
    }

    public byte[] generisiIzvestaj(LocalDate od, LocalDate datDo) throws DocumentException, IOException {
        java.util.List<Pozajmica> pozajmice = pozajmicaRepository.findAllInPeriodWithDetails(od, datDo);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 45, 45, 55, 45);
        PdfWriter writer = PdfWriter.getInstance(doc, baos);

        // Zaglavlje/podnozje na svakoj strani
        writer.setPageEvent(new PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter w, Document d) {
                try {
                    BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, "Cp1250", BaseFont.NOT_EMBEDDED);
                    Font footer = new Font(bf, 8, Font.NORMAL, TEXT_MID);
                    PdfContentByte cb = w.getDirectContent();
                    cb.saveState();
                    cb.setColorFill(BROWN_LIGHT);
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
        Font fSubtitle = new Font(bf,     10, Font.NORMAL, new Color(220, 200, 180));
        Font fSection  = new Font(bfBold, 13, Font.BOLD,   BROWN_DARK);
        Font fTblHead  = new Font(bfBold,  9, Font.BOLD,   WHITE);
        Font fTblCell  = new Font(bf,      9, Font.NORMAL, TEXT_DARK);
        Font fTblSmall = new Font(bf,      8, Font.NORMAL, TEXT_MID);
        Font fMetVal   = new Font(bfBold, 15, Font.BOLD,   BROWN_MED);
        Font fMetLbl   = new Font(bf,      9, Font.NORMAL, TEXT_MID);
        Font fNormal   = new Font(bf,      10, Font.NORMAL, TEXT_DARK);

        // ═══════════════════════════════════════════════════════════
        // HEADER BLOk
        // ═══════════════════════════════════════════════════════════
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        header.setSpacingAfter(20);

        PdfPCell hCell = new PdfPCell();
        hCell.setBackgroundColor(BROWN_MED);
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

        // ═══════════════════════════════════════════════════════════
        // AGGREGACIJE
        // ═══════════════════════════════════════════════════════════
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

        // ═══════════════════════════════════════════════════════════
        // SEKCIJA 1 — AKTIVNOST CLANOVA
        // ═══════════════════════════════════════════════════════════
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
        doc.add(subheading("Top 5 najaktivnijih clanova", new Font(bfBold, 10, Font.BOLD, TEXT_MID)));
        PdfPTable tblClanovi = new PdfPTable(3);
        tblClanovi.setWidthPercentage(100);
        tblClanovi.setSpacingBefore(6);
        tblClanovi.setSpacingAfter(14);
        tblClanovi.setWidths(new float[]{0.5f, 3f, 1.5f});

        addTableHeader(tblClanovi, fTblHead, BROWN_DARK, "#", "Ime i prezime", "Br. pozajmica");
        int rank = 1;
        for (Map.Entry<String, Long> e : topClanovi) {
            Color bg = (rank % 2 == 0) ? BEIGE_LIGHT : WHITE;
            addRow(tblClanovi, fTblCell, bg, Element.ALIGN_CENTER, "" + rank,
                    jmbgToName.getOrDefault(e.getKey(), e.getKey()),
                    "" + e.getValue());
            rank++;
        }
        if (topClanovi.isEmpty()) addEmptyRow(tblClanovi, fTblSmall, 3, "Nema podataka za izabrani period");
        doc.add(tblClanovi);

        // Po kategoriji
        doc.add(subheading("Pozajmice po kategoriji clana", new Font(bfBold, 10, Font.BOLD, TEXT_MID)));
        PdfPTable tblKat = new PdfPTable(3);
        tblKat.setWidthPercentage(70);
        tblKat.setHorizontalAlignment(Element.ALIGN_LEFT);
        tblKat.setSpacingBefore(6);
        tblKat.setSpacingAfter(20);
        tblKat.setWidths(new float[]{3f, 1.5f, 1f});

        addTableHeader(tblKat, fTblHead, BROWN_DARK, "Kategorija", "Br. pozajmica", "%");
        int rowIdx = 1;
        for (Map.Entry<String, Long> e : sortedDesc(poKategoriji)) {
            Color bg = (rowIdx % 2 == 0) ? BEIGE_LIGHT : WHITE;
            String pct = total > 0 ? String.format("%.1f%%", 100.0 * e.getValue() / total) : "—";
            addRow(tblKat, fTblCell, bg, Element.ALIGN_LEFT, labelKategorije(e.getKey()), "" + e.getValue(), pct);
            rowIdx++;
        }
        if (poKategoriji.isEmpty()) addEmptyRow(tblKat, fTblSmall, 3, "Nema podataka");
        doc.add(tblKat);

        // ═══════════════════════════════════════════════════════════
        // SEKCIJA 2 — POPULARNOST NASLOVA
        // ═══════════════════════════════════════════════════════════
        doc.add(sectionHeader("2. Popularnost naslova", fSection, bfBold));

        doc.add(subheading("Top 10 najpozajmljivanijih knjiga", new Font(bfBold, 10, Font.BOLD, TEXT_MID)));
        PdfPTable tblKnjige = new PdfPTable(5);
        tblKnjige.setWidthPercentage(100);
        tblKnjige.setSpacingBefore(6);
        tblKnjige.setSpacingAfter(14);
        tblKnjige.setWidths(new float[]{0.5f, 3.5f, 2f, 1.5f, 1f});

        addTableHeader(tblKnjige, fTblHead, BROWN_DARK, "#", "Naslov", "Autor", "Zanr", "Br.");
        int rk = 1;
        for (Map.Entry<String, Long> e : topKnjige) {
            Color bg = (rk % 2 == 0) ? BEIGE_LIGHT : WHITE;
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
        doc.add(subheading("Pozajmice po zanru", new Font(bfBold, 10, Font.BOLD, TEXT_MID)));
        PdfPTable tblZanr = new PdfPTable(3);
        tblZanr.setWidthPercentage(65);
        tblZanr.setHorizontalAlignment(Element.ALIGN_LEFT);
        tblZanr.setSpacingBefore(6);
        tblZanr.setSpacingAfter(20);
        tblZanr.setWidths(new float[]{3f, 1.5f, 1f});

        addTableHeader(tblZanr, fTblHead, BROWN_DARK, "Zanr", "Br. pozajmica", "%");
        rowIdx = 1;
        for (Map.Entry<String, Long> e : sortedDesc(poZanru)) {
            Color bg = (rowIdx % 2 == 0) ? BEIGE_LIGHT : WHITE;
            String pct = total > 0 ? String.format("%.1f%%", 100.0 * e.getValue() / total) : "—";
            addRow(tblZanr, fTblCell, bg, Element.ALIGN_LEFT, e.getKey(), "" + e.getValue(), pct);
            rowIdx++;
        }
        if (poZanru.isEmpty()) addEmptyRow(tblZanr, fTblSmall, 3, "Nema podataka");
        doc.add(tblZanr);

        // ═══════════════════════════════════════════════════════════
        // SEKCIJA 3 — TRENDOVI CITANJA
        // ═══════════════════════════════════════════════════════════
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
        doc.add(subheading("Mesecni pregled pozajmica", new Font(bfBold, 10, Font.BOLD, TEXT_MID)));
        PdfPTable tblMes = new PdfPTable(3);
        tblMes.setWidthPercentage(65);
        tblMes.setHorizontalAlignment(Element.ALIGN_LEFT);
        tblMes.setSpacingBefore(6);
        tblMes.setSpacingAfter(14);
        tblMes.setWidths(new float[]{2.5f, 1.5f, 1.5f});

        addTableHeader(tblMes, fTblHead, BROWN_DARK, "Mesec", "Br. pozajmica", "Promena");
        rowIdx = 1;
        Long prevCount = null;
        for (Map.Entry<YearMonth, Long> e : mesecniTrend.entrySet()) {
            Color bg = (rowIdx % 2 == 0) ? BEIGE_LIGHT : WHITE;
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

        // Napomena
        Paragraph napomena = new Paragraph(
                "* Izvestaj obuhvata sve fizicke pozajmice evidentirane u sistemu za navedeni period.",
                new Font(bf, 8, Font.ITALIC, TEXT_MID));
        napomena.setSpacingBefore(20);
        doc.add(napomena);

        doc.close();
        return baos.toByteArray();
    }

    // ── Pomocne metode za PDF ────────────────────────────────────────────

    public byte[] generisiIzvestajFonda(LocalDate od, LocalDate datDo) throws DocumentException, IOException {

        // ── DATA FETCHING ────────────────────────────────────────────────
        List<Knjiga>    sveKnjige          = knjigaRepository.findAllActiveWithZanrAndFizicka();
        long            ukupnoPrimeraka    = primerakKnjigeRepository.countAllPrimerci();
        long            dostupnoPrimeraka  = primerakKnjigeRepository.countAvailablePrimerci();
        List<Pozajmica> pozajmiceUPeriodu  = pozajmicaRepository.findAllInPeriodWithDetails(od, datDo);
        List<String>    sveIkadPozajmljene = pozajmicaRepository.findAllEverBorrowedIsbns();
        List<Pozajmica> uPrekoracenjuSad   = pozajmicaRepository.findAllCurrentlyOverdue(LocalDate.now());
        List<Kazna>     izgubljenaPozajmica = kaznaRepository.findAllLostBookKazne();

        // ── SECTION 1 AGGREGATIONS ───────────────────────────────────────
        long ukupnoNaslova = sveKnjige.size();
        long nFizicke = sveKnjige.stream().filter(k -> tip(k, 0)).count();
        long nEKnjige = sveKnjige.stream().filter(k -> tip(k, 1)).count();
        long nAudio   = sveKnjige.stream().filter(k -> tip(k, 2)).count();

        Map<String, Long> poZanruNaslovi = sveKnjige.stream()
            .collect(Collectors.groupingBy(
                k -> k.getZanr() != null ? k.getZanr().getName() : "Bez zanra",
                Collectors.counting()));

        // ── SECTION 2 AGGREGATIONS ───────────────────────────────────────
        Set<String> pozajmljeneIsbnUPeriodu = pozajmiceUPeriodu.stream()
            .map(p -> p.getPrimerakKnjige().getFizickaKnjiga().getIsbn())
            .collect(Collectors.toSet());

        Map<String, Long> cirkulacija = pozajmiceUPeriodu.stream()
            .collect(Collectors.groupingBy(
                p -> p.getPrimerakKnjige().getFizickaKnjiga().getIsbn(),
                Collectors.counting()));

        Map<String, Knjiga> isbnToKnjigaCirc = pozajmiceUPeriodu.stream()
            .collect(Collectors.toMap(
                p -> p.getPrimerakKnjige().getFizickaKnjiga().getIsbn(),
                p -> p.getPrimerakKnjige().getFizickaKnjiga().getKnjiga(),
                (a, b) -> a));

        List<Map.Entry<String, Long>> top10 = cirkulacija.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10).collect(Collectors.toList());

        List<Knjiga> bezCirkulacijeSve = sveKnjige.stream()
            .filter(k -> k.getFizickaKnjiga() != null && !pozajmljeneIsbnUPeriodu.contains(k.getIsbn()))
            .collect(Collectors.toList());
        int ukupnoBezCirk = bezCirkulacijeSve.size();
        List<Knjiga> bezCirkulacijePrikaz = bezCirkulacijeSve.stream().limit(20).collect(Collectors.toList());

        // ── SECTION 3 AGGREGATIONS ───────────────────────────────────────
        Set<String> ikadPozajmljeneSet = new HashSet<>(sveIkadPozajmljene);
        List<Knjiga> nikadPozajmleneSve = sveKnjige.stream()
            .filter(k -> k.getFizickaKnjiga() != null && !ikadPozajmljeneSet.contains(k.getIsbn()))
            .collect(Collectors.toList());
        int ukupnoNikad = nikadPozajmleneSve.size();
        List<Knjiga> nikadPozajmlenePrikaz = nikadPozajmleneSve.stream().limit(15).collect(Collectors.toList());

        // ── PDF SETUP ────────────────────────────────────────────────────
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 45, 45, 55, 45);
        PdfWriter writer = PdfWriter.getInstance(doc, baos);

        writer.setPageEvent(new PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter w, Document d) {
                try {
                    BaseFont bf2 = BaseFont.createFont(BaseFont.HELVETICA, "Cp1250", BaseFont.NOT_EMBEDDED);
                    Font footer2 = new Font(bf2, 8, Font.NORMAL, TEXT_MID);
                    PdfContentByte cb = w.getDirectContent();
                    cb.saveState();
                    cb.setColorFill(BROWN_LIGHT);
                    cb.rectangle(d.left(), d.bottom() - 15, d.right() - d.left(), 1);
                    cb.fill();
                    ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                            new Phrase("Strana " + w.getPageNumber(), footer2),
                            (d.left() + d.right()) / 2, d.bottom() - 25, 0);
                    cb.restoreState();
                } catch (Exception ignored) {}
            }
        });

        doc.open();

        BaseFont bf     = BaseFont.createFont(BaseFont.HELVETICA,      "Cp1250", BaseFont.NOT_EMBEDDED);
        BaseFont bfBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, "Cp1250", BaseFont.NOT_EMBEDDED);

        Font fTitle    = new Font(bfBold, 20, Font.BOLD,   WHITE);
        Font fSubtitle = new Font(bf,     10, Font.NORMAL, new Color(220, 200, 180));
        Font fSection  = new Font(bfBold, 13, Font.BOLD,   BROWN_DARK);
        Font fSectionA = new Font(bfBold, 13, Font.BOLD,   AMBER_DARK);
        Font fTblHead  = new Font(bfBold,  9, Font.BOLD,   WHITE);
        Font fTblCell  = new Font(bf,      9, Font.NORMAL, TEXT_DARK);
        Font fTblSmall = new Font(bf,      8, Font.NORMAL, TEXT_MID);
        Font fMetVal   = new Font(bfBold, 15, Font.BOLD,   BROWN_MED);
        Font fMetLbl   = new Font(bf,      9, Font.NORMAL, TEXT_MID);
        Font fNote     = new Font(bf,      8, Font.ITALIC, TEXT_MID);
        Font fSubSec   = new Font(bfBold, 10, Font.BOLD,   TEXT_MID);

        // ── HEADER ───────────────────────────────────────────────────────
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        header.setSpacingAfter(20);
        PdfPCell hCell = new PdfPCell();
        hCell.setBackgroundColor(BROWN_MED);
        hCell.setPadding(22);
        hCell.setBorder(Rectangle.NO_BORDER);
        Paragraph t1 = new Paragraph(
            "Izvestaj o stanju fonda, cirkulaciji naslova i potrebama za revizijom", fTitle);
        t1.setAlignment(Element.ALIGN_CENTER);
        Paragraph t2 = new Paragraph(
            "Period cirkulacije: " + fmt(od) + "  \u2014  " + fmt(datDo) +
            "    |    Generisano: " + fmt(LocalDate.now()), fSubtitle);
        t2.setAlignment(Element.ALIGN_CENTER);
        t2.setSpacingBefore(5);
        hCell.addElement(t1);
        hCell.addElement(t2);
        header.addCell(hCell);
        doc.add(header);

        // ════════════════════════════════════════════════════════════════
        // SEKCIJA 1 — PREGLED FONDA
        // ════════════════════════════════════════════════════════════════
        doc.add(sectionHeader("1. Pregled fonda", fSection, bfBold));

        PdfPTable metrics1 = new PdfPTable(3);
        metrics1.setWidthPercentage(100);
        metrics1.setSpacingBefore(10);
        metrics1.setSpacingAfter(16);
        metrics1.setWidths(new float[]{1, 1, 1});
        metrics1.addCell(metricCell("" + ukupnoNaslova,    "Ukupno naslova",      fMetVal, fMetLbl, bfBold));
        metrics1.addCell(metricCell("" + ukupnoPrimeraka,  "Ukupno primeraka",    fMetVal, fMetLbl, bfBold));
        metrics1.addCell(metricCell("" + dostupnoPrimeraka,"Dostupnih primeraka", fMetVal, fMetLbl, bfBold));
        doc.add(metrics1);

        doc.add(subheading("Raspodela po formatu", new Font(bfBold, 10, Font.BOLD, TEXT_MID)));
        PdfPTable tblFormat = new PdfPTable(3);
        tblFormat.setWidthPercentage(62);
        tblFormat.setHorizontalAlignment(Element.ALIGN_LEFT);
        tblFormat.setSpacingBefore(6);
        tblFormat.setSpacingAfter(14);
        tblFormat.setWidths(new float[]{3f, 1.5f, 1f});
        addTableHeader(tblFormat, fTblHead, BROWN_DARK, "Format", "Br. naslova", "%");
        String[][] formati = {
            {"Fizicka knjiga", "" + nFizicke,
                ukupnoNaslova > 0 ? String.format("%.1f%%", 100.0 * nFizicke / ukupnoNaslova) : "-"},
            {"E-knjiga",       "" + nEKnjige,
                ukupnoNaslova > 0 ? String.format("%.1f%%", 100.0 * nEKnjige / ukupnoNaslova) : "-"},
            {"Audioknjiga",    "" + nAudio,
                ukupnoNaslova > 0 ? String.format("%.1f%%", 100.0 * nAudio   / ukupnoNaslova) : "-"},
        };
        int rowIdx = 1;
        for (String[] row : formati) {
            addRow(tblFormat, fTblCell, rowIdx % 2 == 0 ? BEIGE_LIGHT : WHITE, Element.ALIGN_LEFT,
                   row[0], row[1], row[2]);
            rowIdx++;
        }
        doc.add(tblFormat);

        doc.add(subheading("Raspodela po zanru", new Font(bfBold, 10, Font.BOLD, TEXT_MID)));
        PdfPTable tblZanrFond = new PdfPTable(3);
        tblZanrFond.setWidthPercentage(70);
        tblZanrFond.setHorizontalAlignment(Element.ALIGN_LEFT);
        tblZanrFond.setSpacingBefore(6);
        tblZanrFond.setSpacingAfter(20);
        tblZanrFond.setWidths(new float[]{3f, 1.5f, 1f});
        addTableHeader(tblZanrFond, fTblHead, BROWN_DARK, "Zanr", "Br. naslova", "%");
        rowIdx = 1;
        for (Map.Entry<String, Long> e : sortedDesc(poZanruNaslovi)) {
            String pct = ukupnoNaslova > 0
                ? String.format("%.1f%%", 100.0 * e.getValue() / ukupnoNaslova) : "-";
            addRow(tblZanrFond, fTblCell, rowIdx % 2 == 0 ? BEIGE_LIGHT : WHITE, Element.ALIGN_LEFT,
                   e.getKey(), "" + e.getValue(), pct);
            rowIdx++;
        }
        if (poZanruNaslovi.isEmpty()) addEmptyRow(tblZanrFond, fTblSmall, 3, "Nema podataka");
        doc.add(tblZanrFond);

        // ════════════════════════════════════════════════════════════════
        // SEKCIJA 2 — CIRKULACIJA NASLOVA
        // ════════════════════════════════════════════════════════════════
        doc.add(sectionHeader(
            "2. Cirkulacija naslova  (" + fmt(od) + " \u2014 " + fmt(datDo) + ")", fSection, bfBold));

        PdfPTable metrics2 = new PdfPTable(3);
        metrics2.setWidthPercentage(100);
        metrics2.setSpacingBefore(10);
        metrics2.setSpacingAfter(16);
        metrics2.setWidths(new float[]{1, 1, 1});
        metrics2.addCell(metricCell("" + pozajmiceUPeriodu.size(),       "Ukupno pozajmica u periodu", fMetVal, fMetLbl, bfBold));
        metrics2.addCell(metricCell("" + pozajmljeneIsbnUPeriodu.size(), "Aktivnih naslova",           fMetVal, fMetLbl, bfBold));
        metrics2.addCell(metricCell("" + ukupnoBezCirk,                  "Naslova bez cirkulacije",    fMetVal, fMetLbl, bfBold));
        doc.add(metrics2);

        doc.add(subheading("Top 10 najpozajmljivanijih naslova", new Font(bfBold, 10, Font.BOLD, TEXT_MID)));
        PdfPTable tblTop = new PdfPTable(5);
        tblTop.setWidthPercentage(100);
        tblTop.setSpacingBefore(6);
        tblTop.setSpacingAfter(14);
        tblTop.setWidths(new float[]{0.5f, 3.5f, 2f, 1.5f, 1f});
        addTableHeader(tblTop, fTblHead, BROWN_DARK, "#", "Naslov", "Autor", "Zanr", "Br. poz.");
        int rk = 1;
        for (Map.Entry<String, Long> e : top10) {
            Color bg = rk % 2 == 0 ? BEIGE_LIGHT : WHITE;
            Knjiga k = isbnToKnjigaCirc.get(e.getKey());
            addRow(tblTop, fTblCell, bg, Element.ALIGN_CENTER,
                "" + rk,
                truncate(k.getNaslov(), 40),
                truncate(k.getAutor() != null ? k.getAutor() : "-", 25),
                k.getZanr() != null ? k.getZanr().getName() : "-",
                "" + e.getValue());
            rk++;
        }
        if (top10.isEmpty()) addEmptyRow(tblTop, fTblSmall, 5, "Nema pozajmica u izabranom periodu");
        doc.add(tblTop);

        doc.add(subheading("Naslovi bez cirkulacije u periodu", new Font(bfBold, 10, Font.BOLD, TEXT_MID)));
        PdfPTable tblBezCirk = new PdfPTable(3);
        tblBezCirk.setWidthPercentage(100);
        tblBezCirk.setSpacingBefore(6);
        tblBezCirk.setSpacingAfter(4);
        tblBezCirk.setWidths(new float[]{3.5f, 2.5f, 2f});
        addTableHeader(tblBezCirk, fTblHead, BROWN_DARK, "Naslov", "Autor", "Zanr");
        rowIdx = 1;
        for (Knjiga k : bezCirkulacijePrikaz) {
            addRow(tblBezCirk, fTblCell, rowIdx % 2 == 0 ? BEIGE_LIGHT : WHITE, Element.ALIGN_LEFT,
                truncate(k.getNaslov(), 45),
                truncate(k.getAutor() != null ? k.getAutor() : "-", 30),
                k.getZanr() != null ? k.getZanr().getName() : "-");
            rowIdx++;
        }
        if (bezCirkulacijePrikaz.isEmpty())
            addEmptyRow(tblBezCirk, fTblSmall, 3, "Svi naslovi su imali cirkulaciju u ovom periodu");
        doc.add(tblBezCirk);
        if (ukupnoBezCirk > 20) {
            Paragraph noteCirc = new Paragraph(
                "* Prikazano prvih 20 od ukupno " + ukupnoBezCirk + " naslova bez cirkulacije u periodu.", fNote);
            noteCirc.setSpacingBefore(4);
            doc.add(noteCirc);
        }
        doc.add(new Paragraph(" "));

        // ════════════════════════════════════════════════════════════════
        // SEKCIJA 3 — POTREBE ZA REVIZIJOM
        // ════════════════════════════════════════════════════════════════
        doc.add(sectionHeader("3. Potrebe za revizijom", fSectionA, bfBold));

        // 3.1 Nikad pozajmljeni
        doc.add(subheading("3.1  Naslovi koji nikada nisu pozajmljeni", fSubSec));
        PdfPTable tblNikad = new PdfPTable(3);
        tblNikad.setWidthPercentage(100);
        tblNikad.setSpacingBefore(6);
        tblNikad.setSpacingAfter(4);
        tblNikad.setWidths(new float[]{3.5f, 2.5f, 2f});
        addTableHeader(tblNikad, fTblHead, BROWN_DARK, "Naslov", "Autor", "Zanr");
        rowIdx = 1;
        for (Knjiga k : nikadPozajmlenePrikaz) {
            addRow(tblNikad, fTblCell, rowIdx % 2 == 0 ? BEIGE_LIGHT : WHITE, Element.ALIGN_LEFT,
                truncate(k.getNaslov(), 45),
                truncate(k.getAutor() != null ? k.getAutor() : "-", 30),
                k.getZanr() != null ? k.getZanr().getName() : "-");
            rowIdx++;
        }
        if (nikadPozajmlenePrikaz.isEmpty())
            addEmptyRow(tblNikad, fTblSmall, 3, "Svi naslovi su imali barem jednu pozajmicu");
        doc.add(tblNikad);
        if (ukupnoNikad > 15) {
            Paragraph noteNikad = new Paragraph(
                "* Prikazano prvih 15 od ukupno " + ukupnoNikad + " naslova koji nikada nisu pozajmljeni.", fNote);
            noteNikad.setSpacingBefore(4);
            doc.add(noteNikad);
        }
        doc.add(new Paragraph(" "));

        // 3.2 Aktivne pozajmice u prekoracenju
        doc.add(subheading("3.2  Aktivne pozajmice u prekoracenju roka", fSubSec));
        PdfPTable tblPrekor = new PdfPTable(4);
        tblPrekor.setWidthPercentage(100);
        tblPrekor.setSpacingBefore(6);
        tblPrekor.setSpacingAfter(14);
        tblPrekor.setWidths(new float[]{3f, 2.5f, 1.5f, 1.5f});
        addTableHeader(tblPrekor, fTblHead, BROWN_DARK, "Naslov", "Clan", "Rok vracanja", "Dana kasnjenja");
        rowIdx = 1;
        for (Pozajmica p : uPrekoracenjuSad) {
            long dana = ChronoUnit.DAYS.between(p.getDatOcVrac(), LocalDate.now());
            String naslov = truncate(
                p.getPrimerakKnjige().getFizickaKnjiga().getKnjiga().getNaslov(), 40);
            String clan = p.getClan().getFirstName() + " " + p.getClan().getLastName();
            addRow(tblPrekor, fTblCell, rowIdx % 2 == 0 ? BEIGE_LIGHT : WHITE, Element.ALIGN_LEFT,
                naslov, truncate(clan, 30), fmt(p.getDatOcVrac()), "+" + dana);
            rowIdx++;
        }
        if (uPrekoracenjuSad.isEmpty())
            addEmptyRow(tblPrekor, fTblSmall, 4, "Nema aktivnih pozajmica u prekoracenju roka");
        doc.add(tblPrekor);

        // 3.3 Izgubljena gradja
        doc.add(subheading("3.3  Izgubljena gradja (evidentirana kazna za gubitak)", fSubSec));
        PdfPTable tblIzg = new PdfPTable(5);
        tblIzg.setWidthPercentage(100);
        tblIzg.setSpacingBefore(6);
        tblIzg.setSpacingAfter(14);
        tblIzg.setWidths(new float[]{3f, 2.5f, 1.5f, 1.2f, 1f});
        addTableHeader(tblIzg, fTblHead, BROWN_DARK, "Naslov", "Clan", "Dat. kazne", "Iznos (RSD)", "Placena");
        rowIdx = 1;
        for (Kazna kaz : izgubljenaPozajmica) {
            String naslov = truncate(
                kaz.getPozajmica().getPrimerakKnjige().getFizickaKnjiga().getKnjiga().getNaslov(), 40);
            String clan = kaz.getClan().getFirstName() + " " + kaz.getClan().getLastName();
            addRow(tblIzg, fTblCell, rowIdx % 2 == 0 ? BEIGE_LIGHT : WHITE, Element.ALIGN_LEFT,
                naslov, truncate(clan, 30), fmt(kaz.getDatNastanka()),
                "" + kaz.getIznosK(), kaz.isPlacena() ? "Da" : "Ne");
            rowIdx++;
        }
        if (izgubljenaPozajmica.isEmpty())
            addEmptyRow(tblIzg, fTblSmall, 5, "Nema evidentiranih izgubljenih primeraka");
        doc.add(tblIzg);

        Paragraph napomena = new Paragraph(
            "* Izvestaj obuhvata aktivni fond (neobrisan). " +
            "Sekcija cirkulacije odnosi se na fizicke pozajmice u navedenom periodu.",
            fNote);
        napomena.setSpacingBefore(20);
        doc.add(napomena);

        doc.close();
        return baos.toByteArray();
    }

    // ── Pomocne metode za PDF ────────────────────────────────────────────

    private boolean tip(Knjiga k, int pos) {
        String t = k.getTipKnjige();
        return t != null && t.length() > pos && t.charAt(pos) == '1';
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
        cell.setBackgroundColor(BEIGE_MED);
        cell.setPadding(14);
        cell.setBorderColor(new Color(220, 200, 190));
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
            cell.setBorderColor(new Color(80, 60, 50));
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
            cell.setBorderColor(new Color(220, 200, 190));
            table.addCell(cell);
            first = false;
        }
    }

    private void addEmptyRow(PdfPTable table, Font font, int colspan, String message) {
        PdfPCell cell = new PdfPCell(new Phrase(message, font));
        cell.setColspan(colspan);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(10);
        cell.setBorderColor(new Color(220, 200, 190));
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
