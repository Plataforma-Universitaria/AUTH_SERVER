package br.ueg.tc.auth.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalutationPhraseDTO {
    private String salutationPhrase;
    private String usernameFieldName;
    private String passwordFieldName;
    private int status;
}
