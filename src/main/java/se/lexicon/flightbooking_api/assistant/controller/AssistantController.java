package se.lexicon.flightbooking_api.assistant.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.lexicon.flightbooking_api.assistant.dto.AssistantChatRequest;
import se.lexicon.flightbooking_api.assistant.dto.AssistantChatResponse;
import se.lexicon.flightbooking_api.assistant.dto.AssistantResponseType;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AssistantController {

    @PostMapping("/chat")
    public ResponseEntity<AssistantChatResponse> chat(
            @Valid @RequestBody AssistantChatRequest request
    ) {
        UUID conversationId = request.conversationId() == null
                ? UUID.randomUUID()
                : request.conversationId();

        AssistantChatResponse response = new AssistantChatResponse(
                conversationId,
                "Assistant integration is ready. You said: "
                        + request.message(),
                AssistantResponseType.TEXT,
                List.of(),
                List.of(),
                List.of(),
                false,
                null
        );

        return ResponseEntity.ok(response);
    }
}