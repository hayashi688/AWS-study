package com.example.awsstudy.controller;
import com.example.awsstudy.dto.*;
import com.example.awsstudy.service.BattleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/battle")
public class BattleController {
    private final BattleService battle;
    public BattleController(BattleService battle) { this.battle = battle; }
    @PostMapping("/answer")
    public AnswerResponse answer(@Valid @RequestBody AnswerRequest request) {
        return battle.answer(request.questionId(), request.answer());
    }
}
