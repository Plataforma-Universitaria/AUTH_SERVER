package br.ueg.tc.auth.server.controller;

import br.ueg.tc.auth.server.dto.LoginRequestDTO;
import br.ueg.tc.auth.server.dto.PlatformAuthResponseDTO;
import br.ueg.tc.auth.server.service.JwtService;
import br.ueg.tc.auth.server.service.PlatformIntegrationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ConcurrentHashMap;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {


    private final PlatformIntegrationService platformIntegrationService;
    @Autowired
    JwtService jwtService;

    @Value("${bot.callback.url}")
    private String botCallbackUrl;

    @Value("${AUTH_MANAGEMENT_KEY:}")
    private String managementKey;

    private ConcurrentHashMap<String, String> jwtStorage = new ConcurrentHashMap<>();

    @GetMapping("/")
    public String loginPage(
            @RequestParam(required = false) String assistenteId,
            Model model,
            HttpSession session) {

        if (assistenteId == null || assistenteId.isEmpty()) {
            assistenteId = (String) session.getAttribute("assistenteId");
        } else {
            session.setAttribute("assistenteId", assistenteId);
        }

        log.info("Página de login iniciada");

        model.addAttribute("assistenteId", assistenteId);
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(

            @ModelAttribute LoginRequestDTO loginRequest,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String assistenteId = (String) session.getAttribute("assistenteId");

        PlatformAuthResponseDTO response = null;

        try {
            if (assistenteId != null && !assistenteId.isEmpty()) {
                loginRequest.setAssistenteId(assistenteId);
            }

            response = platformIntegrationService.authenticateWithPlatform(loginRequest).block();

            if (response != null && response.getResponse() != null) {
                String jwt = jwtService.generateToken(response.getResponse(), loginRequest.getInstitutionName());

                if (assistenteId != null && !assistenteId.isEmpty()) {
                    jwtStorage.put(assistenteId, jwt);
                }

                session.removeAttribute("assistenteId");

                if (botCallbackUrl != null && !botCallbackUrl.isEmpty() && assistenteId != null && !assistenteId.isEmpty()) {
                    return "redirect:" + botCallbackUrl + "?jwt=" + jwt + "&assistenteId=" + assistenteId;
                }

                return "callback";
            }

            String errorMessage = "Credenciais incorretas!";
            if (response != null && response.getMessage() != null && !response.getMessage().isEmpty()) {
                if (!response.getMessage().contains("authenticate")) {
                    errorMessage = response.getMessage();
                }
            }
            redirectAttributes.addFlashAttribute("error", errorMessage);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Credenciais incorretas ou falha de comunicação.");
        }

        return "redirect:/";
    }

    @GetMapping("/token")
    @ResponseBody
    public ResponseEntity<String> getToken(
            @RequestParam String assistenteId,
            @RequestHeader(value = "x-api-key", required = false) String apiKey) {
        if (!hasManagementAccess(apiKey)) {
            return ResponseEntity.status(401).body("Credencial administrativa inválida");
        }
        String token = jwtStorage.get(assistenteId);
        if (token != null) {
            return ResponseEntity.ok(token);
        } else {
            return ResponseEntity.status(404).body("Token não encontrado para assistenteId: " + assistenteId);
        }
    }

    @PostMapping("/logout")
    @ResponseBody
    public ResponseEntity<String> logout(
            @RequestParam String assistenteId,
            @RequestHeader(value = "x-api-key", required = false) String apiKey) {
        if (!hasManagementAccess(apiKey)) {
            return ResponseEntity.status(401).body("Credencial administrativa inválida");
        }
        String token = jwtStorage.get(assistenteId);
        if (token == null) {
            return ResponseEntity.status(404).body("Sessão não encontrada");
        }
        try {
            platformIntegrationService.logoutWithPlatform(token).block();
            jwtStorage.remove(assistenteId);
            return ResponseEntity.ok("Logout concluído");
        } catch (Exception e) {
            log.error("Falha de comunicação no logout institucional");
            return ResponseEntity.status(502).body("Não foi possível concluir o logout");
        }
    }

    private boolean hasManagementAccess(String candidate) {
        if (managementKey == null || managementKey.length() < 32 || candidate == null) {
            return false;
        }
        byte[] expected = managementKey.getBytes(StandardCharsets.UTF_8);
        byte[] actual = candidate.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, actual);
    }
}
