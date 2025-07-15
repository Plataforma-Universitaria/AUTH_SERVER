package br.ueg.tc.auth.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionDTO {
    private String shortName;
    private String longName;
    private List<LoginDataDTO> loginData;
}
