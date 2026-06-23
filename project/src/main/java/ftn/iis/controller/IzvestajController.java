package ftn.iis.controller;

import ftn.iis.service.IzvestajService;
import ftn.iis.utils.JwtService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@RestController
@RequestMapping("/api/izvestaj")
public class IzvestajController {
    private static final Set<String> DOZVOLJENE_ULOGE = Set.of("MENADZER", "BIBLIOTEKAR", "ADMINISTRATOR");
    private static final int BEARER_LEN = 7;

    private final IzvestajService izvestajService;
    private final JwtService jwtService;

    public IzvestajController(IzvestajService izvestajService, JwtService jwtService) {
        this.izvestajService = izvestajService;
        this.jwtService = jwtService;
    }

    @GetMapping("aktivnosti")
    public ResponseEntity<?> generisiIzvestajTeodora(@RequestHeader("Authorization") String authHeader,
    @RequestParam
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate od,
    @RequestParam("do") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datDo){

        try{
            String token = authHeader.substring(BEARER_LEN);
            String rola  = jwtService.extractRole(token);

            if (!DOZVOLJENE_ULOGE.contains(rola)) {
                return ResponseEntity.status(403).body("Nemate pravo pristupa izvestaju.");
            }
            if (od.isAfter(datDo)) {
                return ResponseEntity.badRequest().body("Datum 'od' mora biti pre datuma 'do'.");
            }
            byte[] pdf = izvestajService.generisiIzvestaj(od, datDo);
            String filename = "izvestaj-aktivnosti-"
                    + od.format(DateTimeFormatter.ofPattern("ddMMyyyy"))
                    + "-"
                    + datDo.format(DateTimeFormatter.ofPattern("ddMMyyyy"))
                    + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e){
            return ResponseEntity.internalServerError().body("Greska pri generisanju izvestaja: " + e.getMessage());
        }
    }
}
