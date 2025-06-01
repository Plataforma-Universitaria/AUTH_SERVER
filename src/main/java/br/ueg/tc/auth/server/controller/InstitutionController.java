package br.ueg.tc.auth.server.controller;

import br.ueg.tc.auth.server.dto.InstitutionDTO;
import br.ueg.tc.auth.server.dto.SalutationPhraseDTO;
import br.ueg.tc.auth.server.service.InstitutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InstitutionController {

    private final InstitutionService institutionService;

    @GetMapping("/institutions")
    public ResponseEntity<List<InstitutionDTO>> getAllInstitutions() {
        return ResponseEntity.ok(institutionService.getAllInstitutions());
    }

    @GetMapping("/salutation-phrase/{institution}/{persona}")
    public ResponseEntity<SalutationPhraseDTO> getSalutationPhrase(
            @PathVariable String institution,
            @PathVariable String persona) {
        return ResponseEntity.ok(institutionService.getSalutationPhrase(institution, persona));
    }
}
