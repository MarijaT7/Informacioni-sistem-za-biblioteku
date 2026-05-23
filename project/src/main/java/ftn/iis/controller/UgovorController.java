package ftn.iis.controller;

import ftn.iis.dto.UgovorDetaljniDto;
import ftn.iis.dto.UgovorDto;
import ftn.iis.service.UgovorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ugovori")
public class UgovorController {
    private final UgovorService ugovorService;

    public UgovorController(UgovorService ugovorService){
        this.ugovorService = ugovorService;
    }

    @PostMapping("/kreiranje")
    public ResponseEntity<UgovorDetaljniDto> kreirajUgovor(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UgovorDto dto) {
        String token = authHeader.substring(7);
        UgovorDetaljniDto rezultat = ugovorService.kreirajUgovor(token, dto);
        return ResponseEntity.ok(rezultat);
    }

}
