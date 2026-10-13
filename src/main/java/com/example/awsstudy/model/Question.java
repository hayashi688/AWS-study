package com.example.awsstudy.model;
import java.util.List;
public record Question(String questionId, String service, String enemyStatement, String correctAnswer,
                       List<String> keywords, String guideTitle, String guideText) {}
