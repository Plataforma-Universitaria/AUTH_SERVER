package br.ueg.tc.auth.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {
    private String institutionName;
    private String persona;
    private String username;
    private String password;
    private String assistenteId;
}
