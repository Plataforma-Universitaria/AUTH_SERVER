package br.ueg.tc.auth.server.controller;

import br.ueg.tc.auth.server.dto.LoginRequestDTO;
import br.ueg.tc.auth.server.dto.PlatformAuthResponseDTO;
import br.ueg.tc.auth.server.security.JwtService;
import br.ueg.tc.auth.server.service.PlatformIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final PlatformIntegrationService platformIntegrationService;
    @Autowired
    JwtService jwtService;
    
    @Value("${bot.callback.url}")
    private String botCallbackUrl;

    /**
     * Renderiza a página inicial de login
     */
    @GetMapping("/")
    public String loginPage(@RequestParam(required = false) String assistenteId, Model model) {
        model.addAttribute("assistenteId", assistenteId);
        return "login";
    }

    /**
     * Processa o login do usuário e cria o token jwt
     */
    @PostMapping("/login")
    public String processLogin(
            @ModelAttribute LoginRequestDTO loginRequest,
            @RequestParam(required = false) String assistenteId,
            RedirectAttributes redirectAttributes) {
        
        if (assistenteId != null && !assistenteId.isEmpty()) {
            loginRequest.setAssistenteId(assistenteId);
        }
        
        PlatformAuthResponseDTO response = platformIntegrationService.authenticateWithPlatform(loginRequest)
                .block();
        
        if (response != null && response.getResponse() != null) {
            String jwt = jwtService.generateToken(response.getResponse());

            return "redirect:" + botCallbackUrl + "?jwt=" + jwt;
        } else {
            redirectAttributes.addFlashAttribute("error", 
                    response != null ? response.getMessage() : "Erro de autenticação");
            return "redirect:/?error=true";
        }
    }
    
    /**
     * Endpoint de callback para o bot (pode ser usado para testes ou redirecionamentos alternativos)
     */
    @GetMapping("/callback")
    public String callback(@RequestParam String jwt, Model model) {
        model.addAttribute("jwt", jwt);
        return "callback";
    }
}
