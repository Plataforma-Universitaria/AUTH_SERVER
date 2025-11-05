package br.ueg.tc.auth.server.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/info")
public class InfoController {

    @GetMapping("/terms")
    public String getTerms(HttpSession session, Model model){
        model.addAttribute("assistenteId", session.getAttribute("assistenteId"));
        return "terms";
    }

    @GetMapping("/policy")
    public String getPolicy(HttpSession session, Model model){
        model.addAttribute("assistenteId", session.getAttribute("assistenteId"));
        return "policy";
    }

    @GetMapping("/help")
    public String getHelp(HttpSession session, Model model){
        model.addAttribute("assistenteId", session.getAttribute("assistenteId"));
        return "help";
    }
}