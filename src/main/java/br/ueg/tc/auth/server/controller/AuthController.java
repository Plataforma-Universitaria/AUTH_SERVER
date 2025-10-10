package br.ueg.tc.auth.server.controller;

import br.ueg.tc.auth.server.dto.LoginRequestDTO;
import br.ueg.tc.auth.server.dto.PlatformAuthResponseDTO;
import br.ueg.tc.auth.server.dto.PlatformLogoutResponseDTO;
import br.ueg.tc.auth.server.service.JwtService;
import br.ueg.tc.auth.server.service.PlatformIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final PlatformIntegrationService platformIntegrationService;
    @Autowired
    JwtService jwtService;

    @Value("${bot.callback.url}")
    private String botCallbackUrl;

    private ConcurrentHashMap<String, String> jwtStorage = new ConcurrentHashMap<>();

    @GetMapping("/")
    public String loginPage(
            @RequestParam(required = false) String assistenteId,
            Model model) {
        model.addAttribute("assistenteId", assistenteId);

        return "login";
    }

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

            if (assistenteId != null && !assistenteId.isEmpty()) {
                jwtStorage.put(assistenteId, jwt);
            }

            return "callback";
        } else {
            redirectAttributes.addFlashAttribute("error",
                    response != null ? response.getMessage() : "Erro de autenticação");
            String errorRedirect = "?assistenteId=" + assistenteId + "&error=true";
            return "redirect:/" + errorRedirect;
        }
    }

    @GetMapping("/token")
    @ResponseBody
    public ResponseEntity<String> getToken(@RequestParam String assistenteId) {
        String token = jwtStorage.get(assistenteId);
        if (token != null) {
            return ResponseEntity.ok(token);
        } else {
            return ResponseEntity.status(404).body("Token não encontrado para assistenteId: " + assistenteId);
        }
    }
    @PostMapping("/logout")
    @ResponseBody
    public ResponseEntity<String> logout(@RequestParam String assistenteId) {
        String token = jwtStorage.get(assistenteId);
        PlatformLogoutResponseDTO platformLogoutResponseDTO = platformIntegrationService.logoutWithPlatform(token).block();
        String key = jwtStorage.remove(assistenteId);
        if (key != null) {
            return ResponseEntity.ok(key);
        } else {
            return ResponseEntity.status(404).body("Não encontrado para assistenteId: " + assistenteId);
        }
    }
}
