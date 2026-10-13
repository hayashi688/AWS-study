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
        assertEquals(200, catalog.getQuestions().size());
        for (var q : catalog.getQuestions()) {
            assertFalse(q.enemy().face().isBlank());
            assertFalse(q.enemy().statement().isBlank());
            assertFalse(q.adventureBook().body().isBlank());
            assertTrue(q.adventureBook().officialDocUrl().startsWith("https://docs.aws.amazon.com/"));
            var result = battle.answer(q.id(), q.sampleAnswer());
            assertTrue(result.clear(), q.id());
            assertEquals(0, result.enemyHp());
            assertEquals(q.enemy().maxHp(), result.damage());
            assertTrue(battle.answer(q.id(), q.keywords().get(0)).clear(), q.id());
            assertFalse(battle.answer(q.id(), "わかりません").clear(), q.id());
        }
    }
    @Test void keywordJudgingMatchesOriginalMvp() {
        assertTrue(battle.answer("AWS-001", "オブジェクト").clear());
        assertTrue(battle.answer("AWS-001", "S3はオブジェクトストレージではない").clear());
        assertFalse(battle.answer("AWS-001", "ブロックストレージ").clear());
        assertEquals(49, catalog.getDataset().serviceCatalog().size());
        assertEquals(6, catalog.getDataset().navigationSections().size());
    }
}
