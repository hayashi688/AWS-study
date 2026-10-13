package com.example.awsstudy.service;
import com.example.awsstudy.model.Question;
import org.springframework.stereotype.Service;
/** MVP当初のキーワード包含判定。AIや意味解析は使用しない。 */
@Service
public class KeywordAnswerJudgeService implements AnswerJudgeService {
    public boolean isCorrect(Question question, String answer) {
        // 添付には回答例に指定キーワードが含まれない問題もあるため、回答例そのものも受け付ける。
        return question.keywords().stream().anyMatch(answer::contains)
            || normalize(answer).equals(normalize(question.sampleAnswer()));
    }
    private String normalize(String value) {
        return java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFKC)
            .toLowerCase(java.util.Locale.ROOT).replaceAll("[\\s\\p{Z}]+", "")
            .replaceAll("[。.!！]+$", "").replaceFirst("(?:です|だ)$", "");
    }
}
