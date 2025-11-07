package br.ueg.tc.auth.server.controller;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/info")
@Slf4j
public class InfoController {

    @GetMapping("/terms")
    public String getTerms(HttpSession session, Model model){
        String assistenteId = (String) session.getAttribute("assistenteId");

        if (assistenteId == null || assistenteId.isEmpty()) {
            log.warn("Página de ajuda acessada sem assistenteId na sessão.");
            return "redirect:/";
        }
        model.addAttribute("assistenteId", assistenteId);
        log.info("Página de ajuda carregada com ID da sessão.");
        return "terms";
    }

    @GetMapping("/policy")
    public String getPolicy(HttpSession session, Model model){
        String assistenteId = (String) session.getAttribute("assistenteId");

        if (assistenteId == null || assistenteId.isEmpty()) {
            log.warn("Página de ajuda acessada sem assistenteId na sessão.");
            return "redirect:/";
        }
        model.addAttribute("assistenteId", assistenteId);
        log.info("Página de ajuda carregada com ID da sessão.");
        return "policy";
    }

    @GetMapping("/help")
    public String getHelp(HttpSession session, Model model){
        String assistenteId = (String) session.getAttribute("assistenteId");
        if (assistenteId == null || assistenteId.isEmpty()) {
            log.warn("Página de ajuda acessada sem assistenteId na sessão.");
            return "redirect:/";
        }
        model.addAttribute("assistenteId", assistenteId);
        log.info("Página de ajuda carregada com ID da sessão.");
        return "help";
    }
}