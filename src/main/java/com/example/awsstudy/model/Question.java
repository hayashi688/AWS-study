package com.example.awsstudy.model;
import java.util.List;
public record Question(String id, int stage, String area, String category, String difficulty,
                       EnemyData enemy, String question, AdventureBook adventureBook,
                       String sampleAnswer, List<String> keywords, String judgeNote) {
    public record EnemyData(String name, String face, int maxHp, String statement) {}
    public record AdventureBook(String title, String body, String officialDocUrl, String serviceName, String sectionId, String sectionLabel, String entryTitle) {}
}
