package br.ueg.tc.auth.server.service;

import br.ueg.tc.auth.server.dto.LoginRequestDTO;
import br.ueg.tc.auth.server.dto.PlatformAuthResponseDTO;
import br.ueg.tc.auth.server.dto.PlatformLogoutResponseDTO;
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
    @Value("${platform.logout.url}")
    private String platformLogoutUrl;
    
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
                .onStatus(status -> status.isError(), response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException(errorBody))))
                .bodyToMono(PlatformAuthResponseDTO.class)
                .onErrorResume(e -> {
                    String message = e.getMessage();
                    if (message == null || message.isEmpty()) {
                        message = "Erro desconhecido na plataforma";
                    }
                    return Mono.just(
                        PlatformAuthResponseDTO.builder()
                            .success(false)
                            .message(message)
                            .build()
                    );
                });
    }
    /**
     * Envia o jwt do usuário para a plataforma principal e recebe o UUID
     * @param jwt  Token do usuário
     * @return Resposta da plataforma contendo o UUID
     */
    public Mono<PlatformLogoutResponseDTO> logoutWithPlatform(String jwt) {

        return webClientBuilder.build()
                .post()
                .uri(platformLogoutUrl)
                .header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(PlatformLogoutResponseDTO.class)
                .onErrorResume(e -> Mono.just(
                        PlatformLogoutResponseDTO.builder()
                                .success(false)
                                .message("Erro ao comunicar com a plataforma: " + e.getMessage())
                                .build()
                ));
    }
}
