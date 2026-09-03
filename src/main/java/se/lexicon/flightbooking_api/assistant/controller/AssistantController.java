package se.lexicon.flightbooking_api.assistant.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import se.lexicon.flightbooking_api.assistant.dto.AssistantChatRequest;
import se.lexicon.flightbooking_api.assistant.dto.AssistantChatResponse;
import se.lexicon.flightbooking_api.assistant.service.AssistantService;

@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AssistantController {

    private final AssistantService assistantService;

    @PostMapping("/chat")
    public ResponseEntity<AssistantChatResponse> chat(
            @Valid @RequestBody AssistantChatRequest request,
            Authentication authentication
    ) {
        String ownerKey = getOwnerKey(authentication);

        AssistantChatResponse response =
                assistantService.chat(request, ownerKey);

        return ResponseEntity.ok(response);
    }

    private String getOwnerKey(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication
                instanceof AnonymousAuthenticationToken) {
            return "anonymous";
        }

        return authentication.getName();
    }
}