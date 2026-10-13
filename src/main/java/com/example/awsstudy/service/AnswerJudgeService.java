package com.example.awsstudy.service;
import com.example.awsstudy.model.Question;
public interface AnswerJudgeService {
    boolean isCorrect(Question question, String answer);
}
