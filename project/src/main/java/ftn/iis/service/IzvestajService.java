package ftn.iis.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import ftn.iis.enums.StatusNarudzbine;
import ftn.iis.enums.StatusPredloga;
import ftn.iis.enums.StatusSistemskePreporuke;
import ftn.iis.model.*;
import ftn.iis.repository.*;
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
    private final NarudzbinaRepository narudzbinaRepository;
    private final StavkaNarudzbineRepository stavkaNarudzbineRepository;
    private final ReklamacijaRepository reklamacijaRepository;
    private final BudzetPoZanruRepository budzetPoZanruRepository;
    private final PredlogNabavkaRepository predlogRepository;
    private final FizickaKnjigaRepository fizickaKnjigaRepository;
    private final SistemskePreporukeRepository sistemskePreporukeRepository;

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

    public IzvestajService(PozajmicaRepository pozajmicaRepository,
                           NarudzbinaRepository narudzbinaRepository,
                           StavkaNarudzbineRepository stavkaNarudzbineRepository,
                           ReklamacijaRepository reklamacijaRepository,
                           BudzetPoZanruRepository budzetPoZanruRepository,
                           PredlogNabavkaRepository predlogRepository,
                           FizickaKnjigaRepository fizickaKnjigaRepository,
                           SistemskePreporukeRepository sistemskePreporukeRepository) {
        this.pozajmicaRepository = pozajmicaRepository;
        this.narudzbinaRepository = narudzbinaRepository;
        this.stavkaNarudzbineRepository = stavkaNarudzbineRepository;
        this.reklamacijaRepository = reklamacijaRepository;
        this.budzetPoZanruRepository = budzetPoZanruRepository;
        this.predlogRepository = predlogRepository;
        this.fizickaKnjigaRepository = fizickaKnjigaRepository;
        this.sistemskePreporukeRepository = sistemskePreporukeRepository;

    }





    // ===================================================================================
    //               TEODORA
    // ===================================================================================





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







    // ===================================================================================
    //               MARIJA
    // ===================================================================================






    public byte[] generisiIzvestajNabavka(LocalDate od, LocalDate datDo) throws DocumentException, IOException {

        // Ucitavamm podatke
        List<Narudzbina> narudzbine = narudzbinaRepository.findAllByDatumKreiranjaBetween(od, datDo);
        List<BudzetPoZanru> budzeti = budzetPoZanruRepository.findAll();
        List<PredlogZaNabavku> predlozi = predlogRepository.findAllByDatumPodnosenjaBetween(od, datDo);
        List<SistemskaPreporuka> preporuke = sistemskePreporukeRepository.findAll();
        List<FizickaKnjiga> sveFizicke = fizickaKnjigaRepository.findAll();

        // Generisanje dokumenta, velicine A4, sa marginama 45, 45, 55, 45
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 45, 45, 55, 45);
        PdfWriter writer = PdfWriter.getInstance(doc, baos);


        // Footer - koristim Teodorin
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

        // Samo fontovi, svi koristimo iste
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


        // Header
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        header.setSpacingAfter(20);
        PdfPCell hCell = new PdfPCell();
        hCell.setBackgroundColor(GREEN_DARK);
        hCell.setPadding(22);
        hCell.setBorder(Rectangle.NO_BORDER);
        Paragraph t1 = new Paragraph("Izvestaj o nabavci, fondovima biblioteke i zadovoljenju korisnika", fTitle);
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


        // ================================================
        // Sekcija 1 - Troskovi nabavke
        // ===============================================

        doc.add(sectionHeader("1. Troskovi nabavke", fSection, bfBold));

        // Metrike

        double ukupnoPotrošeno = 0.0;
        double ukupnoKreirano = 0.0;
        double sumaSvihCena = 0.0;
        long brojReklamacija = 0;


        // Mapa za grupisanje po statusu (inicijalizujem sve statuse na 0 radi sigurnosti)
        Map<StatusNarudzbine, Long> poStatusu = new HashMap<>();
        for (StatusNarudzbine status : StatusNarudzbine.values()) {
            poStatusu.put(status, 0L);
        }

        for (Narudzbina n : narudzbine) {

            StatusNarudzbine status = n.getStatus();
            double cena = n.getUkupnaCena();

            sumaSvihCena += cena;

            // Azuriranje brojaca po statusu
            poStatusu.put(status, poStatusu.get(status) + 1);

            // Kategorizacija po statusima
            if (status == StatusNarudzbine.ISPORUCENA || status == StatusNarudzbine.REKLAMIRANA) {
                ukupnoPotrošeno += cena;
            }

            if (status == StatusNarudzbine.KREIRANA) {
                ukupnoKreirano += cena;
            }

            if (status == StatusNarudzbine.REKLAMIRANA) {
                brojReklamacija++;
            }
        }

        // 3. Izracunavanje izvedenih vrednosti nakon petlje
        double stopaReklamacija = narudzbine.isEmpty() ? 0.0 : (100.0 * brojReklamacija / narudzbine.size());
        double prosecnaVrednost = narudzbine.isEmpty() ? 0.0 : (sumaSvihCena / narudzbine.size());

        // 4. Kreiranje PDF tabele
        PdfPTable met1 = new PdfPTable(3);
        met1.setWidthPercentage(100);
        met1.setSpacingBefore(10);
        met1.setSpacingAfter(16);

        met1.addCell(metricCell(String.format("%.2f RSD", ukupnoPotrošeno), "Ukupno potroseno", fMetVal, fMetLbl, bfBold));
        met1.addCell(metricCell(String.format("%.2f RSD", ukupnoKreirano), "U toku (kreirane)", fMetVal, fMetLbl, bfBold));
        met1.addCell(metricCell(
                !narudzbine.isEmpty() ? String.format("%.2f RSD", prosecnaVrednost) : "—",
                "Prosecna vrednost narudzbine", fMetVal, fMetLbl, bfBold));

        doc.add(met1);

        // Dodavanje podnaslova za grupisanje
        doc.add(subheading("Narudzbine po statusu", new Font(bfBold, 10, Font.BOLD, TEXT_MID_GREEN)));

        PdfPTable tblStatus = new PdfPTable(3);
        tblStatus.setWidthPercentage(65);
        tblStatus.setHorizontalAlignment(Element.ALIGN_LEFT);
        tblStatus.setSpacingBefore(6);
        tblStatus.setSpacingAfter(14);
        tblStatus.setWidths(new float[]{3f, 1.5f, 1f});
        addTableHeader(tblStatus, fTblHead, GREEN_DARK, "Status", "Broj", "%");
        int rowIdx = 1;
        for (StatusNarudzbine status : StatusNarudzbine.values()) {
            long count = poStatusu.getOrDefault(status, 0L);
            String pct = narudzbine.isEmpty() ? "—" :
                    String.format("%.1f%%", 100.0 * count / narudzbine.size());
            Color bg = (rowIdx % 2 == 0) ? CARD_LIGHT : WHITE;
            addRow(tblStatus, fTblCell, bg, Element.ALIGN_LEFT,
                    labelStatusa(status.name()), "" + count, pct);
            rowIdx++;
        }
        if (narudzbine.isEmpty()) addEmptyRow(tblStatus, fTblSmall, 3, "Nema narudzbina u periodu");
        doc.add(tblStatus);


        // Dobavljaci
        doc.add(subheading("Top 5 dobavljaca po vrednosti narudzbina", new Font(bfBold, 10, Font.BOLD, TEXT_MID_GREEN)));

        // Grupisanje vrednosti po dobavljaču
        Map<String, Double> poDobavljacu = new HashMap<>();
        for (Narudzbina n : narudzbine) {
            String nazivDobavljaca = n.getDobavljac().getNaziv();
            double trenutnaSuma = poDobavljacu.getOrDefault(nazivDobavljaca, 0.0);
            poDobavljacu.put(nazivDobavljaca, trenutnaSuma + n.getUkupnaCena());
        }

        // Sortiranje mape i uzimanje top 5 elemenata
        List<Map.Entry<String, Double>> topDobavljaci = new ArrayList<>(poDobavljacu.entrySet());
        topDobavljaci.sort(new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> e1, Map.Entry<String, Double> e2) {
                return e2.getValue().compareTo(e1.getValue()); // Opadajući poredak
            }
        });
        if (topDobavljaci.size() > 5) {
            topDobavljaci = topDobavljaci.subList(0, 5);
        }

        PdfPTable tblDob = new PdfPTable(3);
        tblDob.setWidthPercentage(100);
        tblDob.setSpacingBefore(6);
        tblDob.setSpacingAfter(14);
        tblDob.setWidths(new float[]{0.5f, 3.5f, 2f});
        addTableHeader(tblDob, fTblHead, GREEN_DARK, "#", "Dobavljac", "Ukupna vrednost");

        int rnk = 1;
        for (Map.Entry<String, Double> e : topDobavljaci) {
            Color bg = (rnk % 2 == 0) ? CARD_LIGHT : WHITE;
            addRow(tblDob, fTblCell, bg, Element.ALIGN_CENTER,
                    "" + rnk, e.getKey(), String.format("%.2f RSD", e.getValue()));
            rnk++;
        }
        if (topDobavljaci.isEmpty()) addEmptyRow(tblDob, fTblSmall, 3, "Nema podataka");
        doc.add(tblDob);

        // Stopa reklamacija
        doc.add(subheading("Stopa reklamacija", new Font(bfBold, 10, Font.BOLD, TEXT_MID_GREEN)));
        PdfPTable met1b = new PdfPTable(2);
        met1b.setWidthPercentage(60);
        met1b.setHorizontalAlignment(Element.ALIGN_LEFT);
        met1b.setSpacingBefore(10);
        met1b.setSpacingAfter(16);
        met1b.addCell(metricCell("" + brojReklamacija, "Narudzbina sa reklamacijom", fMetVal, fMetLbl, bfBold));
        met1b.addCell(metricCell(String.format("%.1f%%", stopaReklamacija), "Stopa reklamacija", fMetVal, fMetLbl, bfBold));
        doc.add(met1b);


        // ================================================
        // Sekcija 2 - Popunjenost fondova
        // ===============================================


        doc.add(sectionHeader("2. Popunjenost fondova", fSection, bfBold));

        // Primerci po žanru i ukupno primeraka
        Map<String, Long> primerciPoZanru = new HashMap<>();
        long ukupnoPrimeraka = 0;

        for (FizickaKnjiga fk : sveFizicke) {
            String zanr = "Bez zanra";
            if (fk.getKnjiga().getZanr() != null) {
                zanr = fk.getKnjiga().getZanr().getName();
            }

            long brojPrimeraka = 0;
            if (fk.getPrimerci() != null) {
                brojPrimeraka = fk.getPrimerci().size();
            }

            primerciPoZanru.put(zanr, primerciPoZanru.getOrDefault(zanr, 0L) + brojPrimeraka);
            ukupnoPrimeraka += brojPrimeraka;
        }

        PdfPTable met2 = new PdfPTable(2);
        met2.setWidthPercentage(60);
        met2.setHorizontalAlignment(Element.ALIGN_LEFT);
        met2.setSpacingBefore(10);
        met2.setSpacingAfter(16);
        met2.addCell(metricCell("" + sveFizicke.size(), "Naslova u fondu", fMetVal, fMetLbl, bfBold));
        met2.addCell(metricCell("" + ukupnoPrimeraka, "Ukupno primeraka", fMetVal, fMetLbl, bfBold));
        doc.add(met2);

        doc.add(subheading("Primerci po zanru", new Font(bfBold, 10, Font.BOLD, TEXT_MID_GREEN)));
        PdfPTable tblFond = new PdfPTable(3);
        tblFond.setWidthPercentage(70);
        tblFond.setHorizontalAlignment(Element.ALIGN_LEFT);
        tblFond.setSpacingBefore(6);
        tblFond.setSpacingAfter(14);
        tblFond.setWidths(new float[]{3f, 1.5f, 1f});
        addTableHeader(tblFond, fTblHead, GREEN_DARK, "Zanr", "Primeraka", "%");

        rowIdx = 1;
        for (Map.Entry<String, Long> e : sortedDesc(primerciPoZanru)) {
            Color bg = (rowIdx % 2 == 0) ? CARD_LIGHT : WHITE;
            String pct = ukupnoPrimeraka > 0 ?
                    String.format("%.1f%%", 100.0 * e.getValue() / ukupnoPrimeraka) : "—";
            addRow(tblFond, fTblCell, bg, Element.ALIGN_LEFT, e.getKey(), "" + e.getValue(), pct);
            rowIdx++;
        }
        if (primerciPoZanru.isEmpty()) addEmptyRow(tblFond, fTblSmall, 3, "Nema podataka");
        doc.add(tblFond);


        // Knjige sa malo primeraka (filtriranje i sortiranje ručno)
        doc.add(subheading("Knjige sa malo primeraka (manje od 2)", new Font(bfBold, 10, Font.BOLD, TEXT_MID_GREEN)));

        List<FizickaKnjiga> maloPrimeraka = new ArrayList<>();
        for (FizickaKnjiga fk : sveFizicke) {
            if (fk.getPrimerci() == null || fk.getPrimerci().size() < 2) {
                maloPrimeraka.add(fk);
            }
        }

        maloPrimeraka.sort(new Comparator<FizickaKnjiga>() {
            @Override
            public int compare(FizickaKnjiga fk1, FizickaKnjiga fk2) {
                int p1 = fk1.getPrimerci() == null ? 0 : fk1.getPrimerci().size();
                int p2 = fk2.getPrimerci() == null ? 0 : fk2.getPrimerci().size();
                return Integer.compare(p1, p2);
            }
        });

        if (maloPrimeraka.size() > 10) {
            maloPrimeraka = maloPrimeraka.subList(0, 10);
        }

        PdfPTable tblMalo = new PdfPTable(4);
        tblMalo.setWidthPercentage(100);
        tblMalo.setSpacingBefore(6);
        tblMalo.setSpacingAfter(20);
        tblMalo.setWidths(new float[]{3.5f, 2f, 2f, 1f});
        addTableHeader(tblMalo, fTblHead, GREEN_DARK, "Naslov", "Autor", "Zanr", "Primeraka");

        rowIdx = 1;
        for (FizickaKnjiga fk : maloPrimeraka) {
            Color bg = (rowIdx % 2 == 0) ? CARD_LIGHT : WHITE;
            Knjiga k = fk.getKnjiga();
            addRow(tblMalo, fTblCell, bg, Element.ALIGN_LEFT,
                    truncate(k.getNaslov(), 35),
                    truncate(k.getAutor() != null ? k.getAutor() : "—", 20),
                    k.getZanr() != null ? k.getZanr().getName() : "—",
                    "" + (fk.getPrimerci() != null ? fk.getPrimerci().size() : 0));
            rowIdx++;
        }
        if (maloPrimeraka.isEmpty()) addEmptyRow(tblMalo, fTblSmall, 4, "Sve knjige imaju dovoljno primeraka");
        doc.add(tblMalo);

        // Budzet po zanrovima
        doc.add(subheading("Stanje budzeta po zanrovima", new Font(bfBold, 10, Font.BOLD, TEXT_MID_GREEN)));
        PdfPTable tblBudzet = new PdfPTable(5);
        tblBudzet.setWidthPercentage(100);
        tblBudzet.setSpacingBefore(6);
        tblBudzet.setSpacingAfter(20);
        tblBudzet.setWidths(new float[]{2.5f, 2f, 2f, 2f, 1.5f});
        addTableHeader(tblBudzet, fTblHead, GREEN_DARK, "Zanr", "Ukupno", "Potroseno", "Dostupno", "Iskorisc.");

        rowIdx = 1;
        for (BudzetPoZanru b : budzeti) {
            Color bg = (rowIdx % 2 == 0) ? CARD_LIGHT : WHITE;
            double iskorisc = b.getUkupanBudzet() > 0 ? 100.0 * b.getPotroseno() / b.getUkupanBudzet() : 0;
            addRow(tblBudzet, fTblCell, bg, Element.ALIGN_LEFT,
                    b.getZanr().getName(),
                    String.format("%.2f", b.getUkupanBudzet()),
                    String.format("%.2f", b.getPotroseno()),
                    String.format("%.2f", b.getDostupno()),
                    String.format("%.1f%%", iskorisc));
            rowIdx++;
        }
        if (budzeti.isEmpty()) addEmptyRow(tblBudzet, fTblSmall, 5, "Nema definisanog budzeta");
        doc.add(tblBudzet);

        // ==============================================
        //     Sekcija 3 - Zadovoljenje potreba korisnika
        // ==============================================


        doc.add(sectionHeader("3. Zadovoljenje potreba korisnika", fSection, bfBold));

        // Logika -> odbijeni predlozi su oni koji su ili odmah odbijeni od strane bibliotekara ili kasnije odbijeni od strane menadzera
        // Odobreni su samo oni koji su odobreni od strane menadzera, a na cekanju su oni koji su ili odobreni od strane bibliotekara ili jos uvek nisu obradjeni

        long ukupnoPredloga = predlozi.size();
        long odobrenoMenadzer = 0;
        long odbijenoBibliotekar = 0;
        long odbijenoMenadzer = 0;
        long naCekanju = 0;

        Map<String, Long> zanroviIzPredloga = new HashMap<>();

        // Jedan prolaz kroz sve predloge za računanje svih statusa i grupisanje žanrova
        for (PredlogZaNabavku p : predlozi) {
            StatusPredloga status = p.getStatus();

            if (status == StatusPredloga.ODOBRENO_MENADZER) {
                odobrenoMenadzer++;
            } else if (status == StatusPredloga.ODBIJENO_BIBLIOTEKAR) {
                odbijenoBibliotekar++;
            } else if (status == StatusPredloga.ODBIJENO_MENADZER) {
                odbijenoMenadzer++;
            } else if (status == StatusPredloga.NA_CEKANJU || status == StatusPredloga.ODOBRENO_BIBLIOTEKAR) {
                naCekanju++;
            }

            if (p.getZanr() != null) {
                String nazivZanra = p.getZanr().getName();
                zanroviIzPredloga.put(nazivZanra, zanroviIzPredloga.getOrDefault(nazivZanra, 0L) + 1);
            }
        }

        long odbijeno = odbijenoMenadzer + odbijenoBibliotekar;

        PdfPTable met3 = new PdfPTable(4);
        met3.setWidthPercentage(100);
        met3.setSpacingBefore(10);
        met3.setSpacingAfter(16);
        met3.addCell(metricCell("" + ukupnoPredloga, "Ukupno predloga", fMetVal, fMetLbl, bfBold));
        met3.addCell(metricCell("" + odobrenoMenadzer, "Odobreno", fMetVal, fMetLbl, bfBold));
        met3.addCell(metricCell("" + odbijeno, "Odbijeno", fMetVal, fMetLbl, bfBold));
        met3.addCell(metricCell("" + naCekanju, "Na cekanju", fMetVal, fMetLbl, bfBold));
        doc.add(met3);


        // Top zanrovi iz predloga
        doc.add(subheading("Najpopularniji zanrovi iz predloga korisnika", new Font(bfBold, 10, Font.BOLD, TEXT_MID_GREEN)));

        PdfPTable tblZanrPred = new PdfPTable(3);
        tblZanrPred.setWidthPercentage(65);
        tblZanrPred.setHorizontalAlignment(Element.ALIGN_LEFT);
        tblZanrPred.setSpacingBefore(6);
        tblZanrPred.setSpacingAfter(14);
        tblZanrPred.setWidths(new float[]{3f, 1.5f, 1f});
        addTableHeader(tblZanrPred, fTblHead, GREEN_DARK, "Zanr", "Br. predloga", "%");

        rowIdx = 1;
        for (Map.Entry<String, Long> e : sortedDesc(zanroviIzPredloga)) {
            Color bg = (rowIdx % 2 == 0) ? CARD_LIGHT : WHITE;
            String pct = ukupnoPredloga > 0 ? String.format("%.1f%%", 100.0 * e.getValue() / ukupnoPredloga) : "—";
            addRow(tblZanrPred, fTblCell, bg, Element.ALIGN_LEFT, e.getKey(), "" + e.getValue(), pct);
            rowIdx++;
        }
        if (zanroviIzPredloga.isEmpty()) addEmptyRow(tblZanrPred, fTblSmall, 3, "Nema predloga sa definisanim zanrom");
        doc.add(tblZanrPred);


        // Sistemske preporuke — računanje brojaca kroz jednu petlju
        doc.add(subheading("Sistemske preporuke — obrada", new Font(bfBold, 10, Font.BOLD, TEXT_MID_GREEN)));

        long prihvacenoPreporuke = 0;
        long ignorisanoPreporuke = 0;
        long aktivnePreporuke = 0;

        for (SistemskaPreporuka p : preporuke) {
            if (p.getStatus() == StatusSistemskePreporuke.PRIHVACENO) {
                prihvacenoPreporuke++;
            } else if (p.getStatus() == StatusSistemskePreporuke.IGNORISANO) {
                ignorisanoPreporuke++;
            } else if (p.getStatus() == StatusSistemskePreporuke.AKTIVNA) {
                aktivnePreporuke++;
            }
        }

        PdfPTable met3b = new PdfPTable(3);
        met3b.setWidthPercentage(80);
        met3b.setHorizontalAlignment(Element.ALIGN_LEFT);
        met3b.setSpacingBefore(10);
        met3b.setSpacingAfter(20);
        met3b.addCell(metricCell("" + prihvacenoPreporuke, "Prihvaceno", fMetVal, fMetLbl, bfBold));
        met3b.addCell(metricCell("" + ignorisanoPreporuke, "Ignorisano", fMetVal, fMetLbl, bfBold));
        met3b.addCell(metricCell("" + aktivnePreporuke, "Aktivnih (neobradenih)", fMetVal, fMetLbl, bfBold));
        doc.add(met3b);

        doc.close();
        return baos.toByteArray();

    }

    private String labelStatusa(String status) {
        return switch (status) {
            case "KREIRANA"    -> "Kreirana";
            case "ISPORUCENA"  -> "Isporucena";
            case "REKLAMIRANA" -> "Reklamirana";
            case "OTKAZANA"    -> "Otkazana";
            default -> status;
        };
    }









    // ===================================================================================
    //               POMOCNE FUNKCIJE
    // ===================================================================================




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
