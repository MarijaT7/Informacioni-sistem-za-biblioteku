package ftn.iis.controller;

import ftn.iis.dto.DobavljacDto;
import ftn.iis.model.Dobavljac;
import ftn.iis.service.DobavljacService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        Dobavljac dobavljac = dobavljacService.kreirajDobavljaca(token, dobavljacDto);
        return ResponseEntity.ok(dobavljac);
    }
}
