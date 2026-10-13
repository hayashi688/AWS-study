package com.example.awsstudy.controller;
import com.example.awsstudy.dto.*;
import com.example.awsstudy.service.BattleService;
import jakarta.validation.Valid;
import com.example.awsstudy.model.Question;
import com.example.awsstudy.service.QuestionCatalog;
import java.util.List;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/battle")
public class BattleController {
    private final BattleService battle;
    private final QuestionCatalog catalog;
    public BattleController(BattleService battle, QuestionCatalog catalog) { this.battle = battle; this.catalog = catalog; }
    @GetMapping("/dataset")
    public java.util.Map<String, Object> dataset() {
        return java.util.Map.of("datasetVersion", catalog.getDatasetVersion(), "data", catalog.getDataset());
    }
    @GetMapping("/questions")
    public List<Question> questions() { return catalog.getQuestions(); }
    @PostMapping("/answer")
    public AnswerResponse answer(@Valid @RequestBody AnswerRequest request) {
        return battle.answer(request.questionId(), request.answer());
    }
}
