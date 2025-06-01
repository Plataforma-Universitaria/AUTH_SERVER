package br.ueg.tc.auth.server.service;

import br.ueg.tc.auth.server.dto.LoginRequestDTO;
import br.ueg.tc.auth.server.dto.PlatformAuthResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PlatformIntegrationService {

    private final WebClient.Builder webClientBuilder;
    
    @Value("${platform.auth.url}")
    private String platformAuthUrl;
    
    /**
     * Envia as credenciais do usuário para a plataforma principal e recebe o UUID
     * @param loginRequest Dados de login do usuário
     * @return Resposta da plataforma contendo o UUID
     */
    public Mono<PlatformAuthResponseDTO> authenticateWithPlatform(LoginRequestDTO loginRequest) {

        return webClientBuilder.build()
                .post()
                .uri(platformAuthUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .retrieve()
                .bodyToMono(PlatformAuthResponseDTO.class)
                .onErrorResume(e -> Mono.just(
                    PlatformAuthResponseDTO.builder()
                        .success(false)
                        .message("Erro ao comunicar com a plataforma: " + e.getMessage())
                        .build()
                ));
    }
}
