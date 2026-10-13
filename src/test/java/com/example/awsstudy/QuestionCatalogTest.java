package com.example.awsstudy;
import com.example.awsstudy.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class QuestionCatalogTest {
    @Autowired QuestionCatalog catalog;
    @Autowired BattleService battle;
    @Test void allQuestionsCanBeCompletedAndHaveGuideAndEnemy() {
        assertEquals(100, catalog.getQuestions().size());
        for (var q : catalog.getQuestions()) {
            assertFalse(q.enemy().face().isBlank());
            assertFalse(q.enemy().statement().isBlank());
            assertFalse(q.adventureBook().body().isBlank());
            assertTrue(q.adventureBook().officialDocUrl().startsWith("https://docs.aws.amazon.com/"));
            var result = battle.answer(q.id(), q.sampleAnswer());
            assertTrue(result.clear(), q.id());
            assertEquals(0, result.enemyHp());
            assertEquals(q.enemy().maxHp(), result.damage());
            assertFalse(battle.answer(q.id(), q.enemy().statement()).clear(), q.id());
            assertFalse(battle.answer(q.id(), String.join(" ", q.keywords())).clear(), q.id());
            assertFalse(battle.answer(q.id(), q.sampleAnswer() + "とは限らない").clear(), q.id());
            assertFalse(battle.answer(q.id(), q.sampleAnswer() + "。ただし逆が正しい").clear(), q.id());
        }
    }
    @Test void negationAndSubjectReversalAreNotAccepted() {
        for (var answer : new String[]{"S3はオブジェクトストレージではない", "S3はオブジェクトストレージだと思わない",
            "EC2はオブジェクトストレージだ", "オブジェクト", "S3はブロックストレージでオブジェクトも使う"})
            assertFalse(battle.answer("AWS-001", answer).clear(), answer);
        assertFalse(battle.answer("AWS-021", "VPCだけで接続できる").clear());
        assertTrue(battle.answer("AWS-021", "VPCだけでは接続できない").clear());
    }
}
