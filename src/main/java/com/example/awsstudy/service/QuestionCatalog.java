package com.example.awsstudy.service;
import com.example.awsstudy.model.Question;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.URI;
import java.util.*;
@Service
public class QuestionCatalog {
    private final List<Question> questions;
    private final QuestionFile dataset;
    private final String datasetVersion;
    public record QuestionFile(String schemaVersion, int total, List<Question> questions,
        com.fasterxml.jackson.databind.JsonNode serviceCatalog,
        com.fasterxml.jackson.databind.JsonNode navigationSections,
        com.fasterxml.jackson.databind.JsonNode quizSettings) {}
    public QuestionCatalog(ObjectMapper mapper) throws IOException {
        try (var input = new ClassPathResource("aws_rpg_200_questions_adventure_book.json").getInputStream()) {
            var bytes = input.readAllBytes();
            var file = mapper.readValue(bytes, QuestionFile.class);
            dataset = file;
            try { datasetVersion = file.schemaVersion() + "-" + HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes)); }
            catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
            if (file.questions() == null || file.questions().isEmpty() || file.total() != file.questions().size())
                throw new IllegalStateException("問題数とtotalが一致しません");
            questions = file.questions().stream().sorted(Comparator.comparingInt(Question::stage)).toList();
            var ids = new HashSet<String>();
            for (int i = 0; i < questions.size(); i++) {
                var q = questions.get(i);
                if (q.stage() != i + 1 || q.id() == null || q.id().isBlank() || !ids.add(q.id())
                    || q.enemy() == null || q.enemy().maxHp() <= 0 || q.adventureBook() == null
                    || q.sampleAnswer() == null || q.sampleAnswer().isBlank()
                    || q.keywords() == null || q.keywords().isEmpty() || q.keywords().stream().anyMatch(k -> k == null || k.isBlank()))
                    throw new IllegalStateException("問題データが不正です: stage " + (i + 1));
                var url = URI.create(q.adventureBook().officialDocUrl());
                if (!"https".equals(url.getScheme()) || !"docs.aws.amazon.com".equals(url.getHost()) || url.getUserInfo() != null)
                    throw new IllegalStateException("AWS公式資料のURLが不正です");
            }
            if (file.serviceCatalog() == null || !file.serviceCatalog().isArray()
                || file.navigationSections() == null || !file.navigationSections().isArray()
                || file.quizSettings() == null || !file.quizSettings().isObject())
                throw new IllegalStateException("冒険者の書または出題設定が不正です");
            var services = new HashSet<String>();
            for (var service : file.serviceCatalog()) {
                if (service.path("serviceName").asText().isBlank() || !services.add(service.path("serviceName").asText())
                    || !service.path("sections").isArray()) throw new IllegalStateException("サービス一覧が不正です");
                for (var section : service.path("sections")) {
                    if (!section.path("entries").isArray()) throw new IllegalStateException("解説一覧が不正です");
                    for (var entry : section.path("entries")) {
                        var url = URI.create(entry.path("officialDocUrl").asText());
                        if (!"https".equals(url.getScheme()) || !"docs.aws.amazon.com".equals(url.getHost()) || url.getUserInfo() != null)
                            throw new IllegalStateException("解説の公式資料URLが不正です");
                    }
                }
            }
            for (var question : questions) {
                if (!services.contains(question.adventureBook().serviceName()))
                    throw new IllegalStateException("問題に対応するサービスがありません: " + question.id());
            }
        }
    }
    public QuestionFile getDataset() { return dataset; }
    public String getDatasetVersion() { return datasetVersion; }
    public List<Question> getQuestions() { return questions; }
    public Question find(String id) {
        return questions.stream().filter(q -> q.id().equals(id)).findFirst()
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "不明な問題IDです"));
    }
}
