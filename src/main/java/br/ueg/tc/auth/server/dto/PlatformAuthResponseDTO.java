package br.ueg.tc.auth.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformAuthResponseDTO {
    private String uuid;
    private boolean success;
    private String message;
    private String error;
    private String response;
}
