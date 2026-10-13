package com.example.awsstudy.service;
import com.example.awsstudy.model.Question;
import java.text.Normalizer;
import java.util.Locale;
import org.springframework.stereotype.Service;
/** AIなしで意味を推測しない、サンプル回答との限定的な一致判定。 */
@Service
public class KeywordAnswerJudgeService implements AnswerJudgeService {
    public boolean isCorrect(Question question, String answer) {
        return normalize(question.sampleAnswer()).equals(normalize(answer));
    }
    private String normalize(String text) {
        // 末尾の丁寧語と句点、空白、英字の大小・全半角のみを吸収する。
        // 否定語、助詞、語順、追加の文は削除しない（意味の逆転を防ぐ）。
        String value = Normalizer.normalize(text, Normalizer.Form.NFKC)
            .toLowerCase(Locale.ROOT).replaceAll("[\\s\\p{Z}]+", "")
            .replaceAll("[。.!！]+$", "");
        return value.replaceFirst("(?:です|だ)$", "");
    }
}
