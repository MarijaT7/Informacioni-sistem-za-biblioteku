package ftn.iis.controller;

import ftn.iis.dto.CetSesijaDetaljnoDto;
import ftn.iis.dto.CetSesijaOsnovnoDto;
import ftn.iis.dto.NovaCetSesijaDto;
import ftn.iis.service.CetPorukaService;
import ftn.iis.service.CetSesijaService;
import ftn.iis.service.OcenaCetPorukeService;
import ftn.iis.utils.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/cet-sesija")
public class CetSesijaController {
    private final CetSesijaService cetSesijaService;
    private final CetPorukaService cetPorukaService;
    private final OcenaCetPorukeService ocenaCetPorukeService;
    private final JwtService jwtService;
    private static final int BEARER_PREFIX_LENGTH = 7;

    public CetSesijaController(CetSesijaService cetSesijaService, CetPorukaService cetPorukaService, OcenaCetPorukeService ocenaCetPorukeService, JwtService jwtService) {
        this.cetSesijaService = cetSesijaService;
        this.cetPorukaService = cetPorukaService;
        this.ocenaCetPorukeService = ocenaCetPorukeService;
        this.jwtService = jwtService;
    }

    @GetMapping("/sve")
    public ResponseEntity<List<CetSesijaOsnovnoDto>> ispisiSveCetSesije(@RequestHeader(value = "Authorization", required = false) String authHeader){
        String role = safeExtractRole(authHeader);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!isRoleAllowed(role, "CLAN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String jmbg = safeExtractJmbg(authHeader);
        if (jmbg == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<CetSesijaOsnovnoDto> rezultat = cetSesijaService.ispisiSveCetSesije(jmbg);
        return ResponseEntity.ok(rezultat);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CetSesijaDetaljnoDto> getCetSesijaPoId(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id
    ) {
        String role = safeExtractRole(authHeader);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!isRoleAllowed(role, "CLAN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String jmbg = safeExtractJmbg(authHeader);
        if (jmbg == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return cetSesijaService.getCetSesijaPoId(jmbg, id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/nova")
    public ResponseEntity<?> postNovaCetSesija(
            @RequestHeader("Authorization") String authHeader,
            @RequestPart("podaci") NovaCetSesijaDto podaci) {
        String role = safeExtractRole(authHeader);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!isRoleAllowed(role, "CLAN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String jmbg = safeExtractJmbg(authHeader);
        if (jmbg == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (podaci.getSadrzajPoruke() == null || podaci.getSadrzajPoruke().isBlank()) {
            return ResponseEntity.badRequest().body("Prva poruka mora da ima sadržaj!");
        }
        if (podaci.getTipAgentaCS() == null) {
            return ResponseEntity.badRequest().body("Obavezan je izbor tipa agenta!");
        }

        try {
            CetSesijaDetaljnoDto cetSesijaDetaljnoDto = CetSesijaDetaljnoDto.fromCetSesija(cetSesijaService.postNovaCetSesija(jmbg, podaci));
            return ResponseEntity.status(HttpStatus.CREATED).body(cetSesijaDetaljnoDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Greška prilikom kreiranja čet sesije: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCetSesija(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        String role = safeExtractRole(authHeader);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!isRoleAllowed(role, "CLAN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String jmbg = safeExtractJmbg(authHeader);
        if (jmbg == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            cetSesijaService.deleteCetSesija(jmbg, id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Greška prilikom brisanja čet sesije: " + e.getMessage());
        }
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(BEARER_PREFIX_LENGTH);
    }

    private String safeExtractRole(String authHeader) {
        try {
            String token = extractToken(authHeader);
            if (token == null) {
                return null;
            }
            return jwtService.extractRole(token);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isRoleAllowed(String role, String... allowedRoles) {
        if (role == null) {
            return false;
        }
        for (String allowed : allowedRoles) {
            if (role.equalsIgnoreCase(allowed)) {
                return true;
            }
        }
        return false;
    }

    private String safeExtractJmbg(String authHeader) {
        try {
            String token = extractToken(authHeader);
            if (token == null) {
                return null;
            }
            return jwtService.extractJmbg(token);
        } catch (Exception e) {
            return null;
        }
    }
}
