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
    public record QuestionFile(String schemaVersion, int total, List<Question> questions) {}
    public QuestionCatalog(ObjectMapper mapper) throws IOException {
        try (var input = new ClassPathResource("aws_rpg_100_questions.json").getInputStream()) {
            var file = mapper.readValue(input, QuestionFile.class);
            if (file.questions() == null || file.questions().isEmpty() || file.total() != file.questions().size())
                throw new IllegalStateException("問題数とtotalが一致しません");
            questions = file.questions().stream().sorted(Comparator.comparingInt(Question::stage)).toList();
            var ids = new HashSet<String>();
            for (int i = 0; i < questions.size(); i++) {
                var q = questions.get(i);
                if (q.stage() != i + 1 || q.id() == null || q.id().isBlank() || !ids.add(q.id())
                    || q.enemy() == null || q.enemy().maxHp() <= 0 || q.adventureBook() == null
                    || q.sampleAnswer() == null || q.sampleAnswer().isBlank())
                    throw new IllegalStateException("問題データが不正です: stage " + (i + 1));
                var url = URI.create(q.adventureBook().officialDocUrl());
                if (!"https".equals(url.getScheme()) || !"docs.aws.amazon.com".equals(url.getHost()) || url.getUserInfo() != null)
                    throw new IllegalStateException("AWS公式資料のURLが不正です");
            }
        }
    }
    public List<Question> getQuestions() { return questions; }
    public Question find(String id) {
        return questions.stream().filter(q -> q.id().equals(id)).findFirst()
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "不明な問題IDです"));
    }
}
