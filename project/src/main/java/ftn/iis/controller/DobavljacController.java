package ftn.iis.controller;

import ftn.iis.dto.DobavljacDetaljniDto;
import ftn.iis.dto.DobavljacDto;
import ftn.iis.dto.DobavljacIzmenaDto;
import ftn.iis.dto.OsnovniDobavljacDto;
import ftn.iis.model.Dobavljac;
import ftn.iis.service.DobavljacService;
import org.aspectj.lang.annotation.DeclareError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dobavljaci")
public class DobavljacController {
    private final DobavljacService dobavljacService;

    public DobavljacController(DobavljacService dobavljacService){
        this.dobavljacService = dobavljacService;
    }

    @PostMapping("/unos")
    public ResponseEntity<?> kreirajDobavljaca(@RequestHeader ("Authorization")
                                                   String authHeader, @RequestBody DobavljacDto dobavljacDto){
        String token = authHeader.substring(7);
        DobavljacDetaljniDto dobavljacDetaljni = dobavljacService.kreirajDobavljaca(token, dobavljacDto);
        return ResponseEntity.ok(dobavljacDetaljni);
    }

    @GetMapping("/prikaz-svih")
    public ResponseEntity<?> ispisiSve(@RequestHeader ("Authorization")
                                               String authHeader){
        String token = authHeader.substring(7);
        List<OsnovniDobavljacDto> dobavljaci = dobavljacService.ispisiSve(token);
        return ResponseEntity.ok(dobavljaci);
    }

    @GetMapping("/detaljan-prikaz/{id}")
    public ResponseEntity<?> ispisiJednog(@RequestHeader ("Authorization")
                                       String authHeader, @PathVariable Long id){
        String token = authHeader.substring(7);
        DobavljacDetaljniDto dobavljacDetaljniDto = dobavljacService.ispisiJednog(token, id);
        return ResponseEntity.ok(dobavljacDetaljniDto);
    }

    @PatchMapping("/izmena/{id}")
    public ResponseEntity<?> izmeni(@RequestHeader ("Authorization")
                                          String authHeader, @PathVariable Long id, @RequestBody DobavljacIzmenaDto dto){
        String token = authHeader.substring(7);
        DobavljacDetaljniDto dobavljacDetaljniDto = dobavljacService.izmeni(token, id, dto);
        return ResponseEntity.ok(dobavljacDetaljniDto);
    }

    @DeleteMapping("/brisanje/{id}")
    public ResponseEntity<?> obrisi(@RequestHeader ("Authorization") String authHeader, @PathVariable Long id){
        String token = authHeader.substring(7);
        DobavljacDetaljniDto dobavljacDetaljniDto = dobavljacService.obrisi(token, id);
        return ResponseEntity.ok(dobavljacDetaljniDto);
    }
}
