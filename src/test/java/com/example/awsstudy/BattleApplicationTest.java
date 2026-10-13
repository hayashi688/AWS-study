package com.example.awsstudy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest
@AutoConfigureMockMvc
class BattleApplicationTest {
    @Autowired MockMvc mvc;
    @Test void rendersGameAndGuide() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("game"))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("S3 スライム")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("冒険者の書")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("value=\"100\"")));
    }
    @Test void wrongThenCorrectAnswer() throws Exception {
        mvc.perform(post("/api/battle/answer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"questionId\":\"AWS-001\",\"answer\":\"ブロックストレージです\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.result").value("WRONG"))
            .andExpect(jsonPath("$.damage").value(0)).andExpect(jsonPath("$.enemyHp").value(100))
            .andExpect(jsonPath("$.clear").value(false));
        for (String answer : new String[]{"S3はオブジェクトストレージです", "Ｓ３はオブジェクトストレージだ！"}) {
            mvc.perform(post("/api/battle/answer").contentType(MediaType.APPLICATION_JSON)
                .content("{\"questionId\":\"AWS-001\",\"answer\":\"" + answer + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.result").value("CORRECT"))
                .andExpect(jsonPath("$.damage").value(100)).andExpect(jsonPath("$.enemyHp").value(0))
                .andExpect(jsonPath("$.clear").value(true));
        }
    }
    @Test void invalidRequestsAreRejected() throws Exception {
        for (String body : new String[]{"{\"questionId\":\"unknown\",\"answer\":\"オブジェクト\"}",
            "{\"questionId\":\"AWS-001\",\"answer\":\" \"}", "{}"}) {
            mvc.perform(post("/api/battle/answer").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        }
    }
}
