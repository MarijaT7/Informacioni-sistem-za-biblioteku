package ftn.iis.controller;

import ftn.iis.dto.NovaCetPorukaDto;
import ftn.iis.dto.NovaCetPorukaOdgovorDto;
import ftn.iis.dto.OcenaCetPorukeDto;
import ftn.iis.exception.VektorskiServisException;
import ftn.iis.model.OcenaCetPoruke;
import ftn.iis.service.CetPorukaService;
import ftn.iis.service.OcenaCetPorukeService;
import ftn.iis.utils.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/cet-poruka")
public class CetPorukaController {
    private final CetPorukaService cetPorukaService;
    private final OcenaCetPorukeService ocenaCetPorukeService;
    private final JwtService jwtService;
    private static final int BEARER_PREFIX_LENGTH = 7;

    public CetPorukaController(CetPorukaService cetPorukaService, OcenaCetPorukeService ocenaCetPorukeService, JwtService jwtService) {
        this.cetPorukaService = cetPorukaService;
        this.ocenaCetPorukeService = ocenaCetPorukeService;
        this.jwtService = jwtService;
    }

    @PostMapping("/cet-sesija/{idCetSesije}")
    public ResponseEntity<?> postNovaCetPoruka(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long idCetSesije,
            @RequestBody NovaCetPorukaDto podaci) {

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
            return ResponseEntity.badRequest().body("Poruka mora da ima sadržaj!");
        }

        try {
            NovaCetPorukaOdgovorDto odgovor = cetPorukaService.postNovaCetPoruka(jmbg, idCetSesije, podaci.getSadrzajPoruke());
            return ResponseEntity.status(HttpStatus.CREATED).body(odgovor);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (VektorskiServisException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Agent trenutno nije dostupan: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Greška prilikom slanja poruke: " + e.getMessage());
        }
    }

    @GetMapping("/{idCetPoruke}/ocena")
    public ResponseEntity<?> getOcenaCetPoruke(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long idCetPoruke) {

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
            return ocenaCetPorukeService.getOcenaCetPoruke(jmbg, idCetPoruke)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.noContent().build());
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Greška prilikom dobavljanja ocene: " + e.getMessage());
        }
    }

    @PostMapping("/{idCetPoruke}/ocena")
    public ResponseEntity<?> postOcenaCetPoruke(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long idCetPoruke,
            @RequestBody OcenaCetPorukeDto podaci) {

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
            return ResponseEntity.status(HttpStatus.CREATED).body(ocenaCetPorukeService.ocenaCetPoruke(jmbg, idCetPoruke, podaci).get());
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Greška prilikom ocenjivanja poruke: " + e.getMessage());
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
