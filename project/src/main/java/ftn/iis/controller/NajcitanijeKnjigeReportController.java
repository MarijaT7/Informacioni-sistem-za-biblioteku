package ftn.iis.controller;

import ftn.iis.dto.NajcitanijaKnjigaDto;
import ftn.iis.service.NajcitanijeKnjigeReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Izvestaj: najcitanije knjige odredjenog zanra u poslednjih 30 dana.
 *
 * Upit koji stoji iza ovog endpointa je predmet zadatka "Optimizacija
 * kreiranog upita upotrebom indeksa" - videti:
 * - KnjigaRepository#findNajcitanijeKnjigePoZanru (sam upit)
 * - db/migration/V1__add_indexes_for_najcitanije_knjige_upit.sql (indeksi)
 * - generate_test_data.sql (skripta za generisanje test podataka radi
 *   merenja EXPLAIN ANALYZE plana pre/posle indeksa)
 */
@RestController
@RequestMapping("/api/izvestaji/najcitanije-knjige")
public class NajcitanijeKnjigeReportController {

    private final NajcitanijeKnjigeReportService reportService;

    public NajcitanijeKnjigeReportController(NajcitanijeKnjigeReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * GET /api/izvestaji/najcitanije-knjige/{zanrId}
     *
     * Vraca listu knjiga zadatog zanra sortiranu po broju citanja u
     * poslednjih 30 dana (opadajuce). Dostupno bibliotekarima, menadzerima
     * i administratorima - ovo je interni analiticki izvestaj, ne
     * javni podatak za obicne clanove.
     */
    @GetMapping("/{zanrId}")
    @PreAuthorize("hasAnyRole('BIBLIOTEKAR', 'MENADZER', 'ADMINISTRATOR')")
    public ResponseEntity<List<NajcitanijaKnjigaDto>> getNajcitanijeKnjige(@PathVariable Long zanrId) {
        return ResponseEntity.ok(reportService.getNajcitanijeKnjigePoZanru(zanrId));
    }
}
