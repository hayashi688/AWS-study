package com.example.awsstudy.controller;
import com.example.awsstudy.service.BattleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class GameController {
    private final BattleService battle;
    public GameController(BattleService battle) { this.battle = battle; }
    @GetMapping("/")
    public String game(Model model) {
        model.addAttribute("enemy", battle.getEnemy());
        model.addAttribute("question", battle.getQuestion());
        return "game";
    }
}
