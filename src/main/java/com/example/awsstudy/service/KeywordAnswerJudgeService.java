package com.example.awsstudy.service;
import com.example.awsstudy.model.Question;
import org.springframework.stereotype.Service;
@Service
public class KeywordAnswerJudgeService implements AnswerJudgeService {
    public boolean isCorrect(Question question, String answer) {
        return question.keywords().stream().anyMatch(answer::contains);
    }
}
