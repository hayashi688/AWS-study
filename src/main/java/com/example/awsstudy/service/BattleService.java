package com.example.awsstudy.service;
import com.example.awsstudy.dto.AnswerResponse;
import com.example.awsstudy.model.Enemy;
import com.example.awsstudy.model.Question;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
@Service
public class BattleService {
    private final AnswerJudgeService judge;
    private final Question question = new Question("S3-001", "Amazon S3",
        "S3はEC2に接続して使用するブロックストレージだ！",
        "Amazon S3はオブジェクトストレージサービスである",
        List.of("オブジェクト", "オブジェクトストレージ"), "Amazon S3とは？",
        "Amazon S3はオブジェクトストレージサービスです。データは「オブジェクト」として「バケット」に保存されます。");
    public BattleService(AnswerJudgeService judge) { this.judge = judge; }
    public Question getQuestion() { return question; }
    public Enemy getEnemy() { return new Enemy("s3-slime", "S3スライム", 100, 100, question.enemyStatement()); }
    // MVPは保存なし。各回答を初期HPから判定し、クリア後の操作は画面側で止める。
    public AnswerResponse answer(String questionId, String answer) {
        if (!question.questionId().equals(questionId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不明な問題IDです");
        }
        if (judge.isCorrect(question, answer)) {
            return new AnswerResponse("CORRECT", 100, Math.max(0, getEnemy().currentHp() - 100), "ぐっ……正解だ……！", true);
        }
        return new AnswerResponse("WRONG", 0, getEnemy().currentHp(),
            "それでは俺を倒せないぞ！冒険者の書を調べてみろ！", false);
    }
}
