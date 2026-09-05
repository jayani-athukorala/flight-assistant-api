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

import java.util.UUID;

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

    @PostMapping("/conversations/{conversationId}/actions/{actionId}/confirm")
    public ResponseEntity<AssistantChatResponse> confirmAction(
            @PathVariable UUID conversationId,
            @PathVariable UUID actionId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                assistantService.confirmAction(
                        conversationId,
                        actionId,
                        getRequiredOwnerKey(authentication))
        );
    }

    @DeleteMapping("/conversations/{conversationId}/actions/{actionId}")
    public ResponseEntity<Void> rejectAction(
            @PathVariable UUID conversationId,
            @PathVariable UUID actionId,
            Authentication authentication
    ) {
        assistantService.rejectAction(
                conversationId,
                actionId,
                getRequiredOwnerKey(authentication));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<Void> clearConversation(
            @PathVariable UUID conversationId,
            Authentication authentication
    ) {
        assistantService.clearConversation(
                conversationId,
                getOwnerKey(authentication));
        return ResponseEntity.noContent().build();
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

    private String getRequiredOwnerKey(Authentication authentication) {
        String ownerKey = getOwnerKey(authentication);
        if ("anonymous".equals(ownerKey)) {
            throw new org.springframework.security.authentication
                    .AuthenticationCredentialsNotFoundException(
                    "Authentication is required for this action");
        }
        return ownerKey;
    }
}
