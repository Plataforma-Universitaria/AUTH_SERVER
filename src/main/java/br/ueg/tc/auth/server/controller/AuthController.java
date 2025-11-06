package br.ueg.tc.auth.server.controller;

import br.ueg.tc.auth.server.dto.LoginRequestDTO;
import br.ueg.tc.auth.server.dto.PlatformAuthResponseDTO;
import br.ueg.tc.auth.server.dto.PlatformLogoutResponseDTO;
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

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequiredArgsConstructor
@Slf4j
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
            Model model,
            HttpSession session) {
        log.info("loginPage ativado para chat id iniciado em " + assistenteId.substring(0, 4));

        if (assistenteId == null || assistenteId.isEmpty()) {
            assistenteId = (String) session.getAttribute("assistenteId");
        } else {
            session.setAttribute("assistenteId", assistenteId);
        }

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
                String jwt = jwtService.generateToken(response.getResponse());

                if (assistenteId != null && !assistenteId.isEmpty()) {
                    jwtStorage.put(assistenteId, jwt);
                }

                session.removeAttribute("assistenteId");
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
        try{
            String token = jwtStorage.get(assistenteId);

        log.info("Efetuando logout para chat id iniciado em {}", assistenteId.substring(0, 4));
        PlatformLogoutResponseDTO platformLogoutResponseDTO = platformIntegrationService.logoutWithPlatform(token).block();
        String key = jwtStorage.remove(assistenteId);
        if (key != null) {
            log.info("Retornou key");
            return ResponseEntity.ok(key);

        } else {
            log.info("Retornou 404");
            return ResponseEntity.status(404).body("Não encontrado para assistenteId: " + assistenteId);

        }
        }catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(404).body("Não encontrado para assistenteId: " + assistenteId);
        }
    }
}
