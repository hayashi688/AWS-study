package com.example.awsstudy.controller;
import com.example.awsstudy.service.QuestionCatalog;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class GameController {
    private final QuestionCatalog catalog;
    public GameController(QuestionCatalog catalog) { this.catalog = catalog; }
    @GetMapping("/")
    public String game(Model model) {
        model.addAttribute("question", catalog.getQuestions().get(0));
        model.addAttribute("total", catalog.getQuestions().size());
        return "game";
    }
}
