package com.lounge.domain.visitpass.controller;

import com.lounge.domain.visitpass.exception.VisitPassException;
import com.lounge.domain.visitpass.service.VisitPassService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/visit-passes/public")
@RequiredArgsConstructor
public class VisitPassPublicController {

    private final VisitPassService visitPassService;

    @GetMapping("/{publicToken}")
    public String getPublicVisitPass(
            @PathVariable String publicToken,
            Model model,
            HttpServletResponse response
    ) {
        try {
            model.addAttribute("view", visitPassService.getPublicView(publicToken));
            return "visit-pass/public";
        } catch (VisitPassException e) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return "visit-pass/not-found";
        }
    }
}
