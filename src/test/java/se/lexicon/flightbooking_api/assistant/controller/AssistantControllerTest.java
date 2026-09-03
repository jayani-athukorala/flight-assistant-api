package se.lexicon.flightbooking_api.assistant.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AssistantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void chatCreatesConversationForFirstMessage() throws Exception {
        mockMvc.perform(post("/api/assistant/chat")
                        .with(user("user@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "conversationId": null,
                          "message": "Find flights to Paris"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").isNotEmpty())
                .andExpect(jsonPath("$.type").value("TEXT"))
                .andExpect(jsonPath("$.flights").isArray())
                .andExpect(jsonPath("$.availableSeats").isArray())
                .andExpect(jsonPath("$.bookings").isArray())
                .andExpect(jsonPath("$.requiresConfirmation").value(false))
                .andExpect(jsonPath("$.pendingAction").doesNotExist());
    }

    @Test
    void chatKeepsExistingConversationId() throws Exception {
        String conversationId =
                "cf56fbf3-c39a-46bc-8252-3716246fbfa0";

        mockMvc.perform(post("/api/assistant/chat")
                        .with(user("user@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "conversationId": "cf56fbf3-c39a-46bc-8252-3716246fbfa0",
                          "message": "Show morning flights"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId")
                        .value(conversationId));
    }

    @Test
    void chatRejectsEmptyMessage() throws Exception {
        mockMvc.perform(post("/api/assistant/chat")
                        .with(user("user@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "conversationId": null,
                          "message": ""
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

}