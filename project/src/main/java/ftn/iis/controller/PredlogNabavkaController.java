package ftn.iis.controller;

import ftn.iis.dto.PredlogNabavkaDto;
import ftn.iis.dto.PredlogNabavkaResponseDto;
import ftn.iis.service.PredlogNabavkeService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/predlozi")
public class PredlogNabavkaController {
    private final PredlogNabavkeService predlogNabavkeService;

    public PredlogNabavkaController(PredlogNabavkeService predlogNabavkeService){
        this.predlogNabavkeService = predlogNabavkeService;
    }

    @PostMapping("/kreiraj")
    public ResponseEntity<PredlogNabavkaResponseDto> kreirajPredlog(@RequestHeader("Authorization") String authHeader,
                                                                    @RequestBody PredlogNabavkaDto predlogNabavkaDto){
        String token = authHeader.substring(7);
        PredlogNabavkaResponseDto predlog = predlogNabavkeService.kreirajPredlog(token, predlogNabavkaDto);
        return ResponseEntity.ok(predlog);
    }
}
