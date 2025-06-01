package br.ueg.tc.auth.server.service;

import br.ueg.tc.auth.server.dto.InstitutionDTO;
import br.ueg.tc.auth.server.dto.SalutationPhraseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InstitutionService {

    private final WebClient webClient = WebClient.create();

    @Value("${platform.institutions.url}")
    private String institutionsUrl;

    @Value("${platform.salutation.url}")
    private String salutationUrl;

    public List<InstitutionDTO> getAllInstitutions() {
        try {
            return webClient.get()
                    .uri(institutionsUrl)
                    .retrieve()
                    .bodyToFlux(InstitutionDTO.class)
                    .collectList()
                    .block();
        } catch (Exception e) {
            e.printStackTrace(); // ou log com SLF4J
            return Collections.emptyList();
        }
    }

    public SalutationPhraseDTO getSalutationPhrase(String institution, String persona) {
        try {
            String url = salutationUrl + institution.toUpperCase() + "/" + persona.toUpperCase();
            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(SalutationPhraseDTO.class)
                    .block();
        } catch (Exception e) {
            e.printStackTrace();
            return SalutationPhraseDTO.builder().status(404).build();
        }
    }
}
