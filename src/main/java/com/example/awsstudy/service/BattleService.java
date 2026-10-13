package com.example.awsstudy.service;
import com.example.awsstudy.dto.AnswerResponse;
import com.example.awsstudy.model.Question;
import org.springframework.stereotype.Service;
@Service
public class BattleService {
    private final AnswerJudgeService judge;
    private final QuestionCatalog catalog;
    public BattleService(AnswerJudgeService judge, QuestionCatalog catalog) {
        this.judge = judge; this.catalog = catalog;
    }
    public Question getQuestion() { return catalog.getQuestions().get(0); }
    public AnswerResponse answer(String questionId, String answer) {
        var question = catalog.find(questionId);
        if (judge.isCorrect(question, answer))
            return new AnswerResponse("CORRECT", question.enemy().maxHp(), 0, "ぐっ……正解だ……！", true);
        return new AnswerResponse("WRONG", 0, question.enemy().maxHp(),
            "正しい内容を確認できなかったぞ。冒険者の書と回答例を調べ、もう一度答えてみろ！", false);
    }
}
