package se.lexicon.flightbooking_api.assistant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties(prefix = "openai")
public record OpenAiProperties(
        @DefaultValue("false")
        boolean enabled,

        String apiKey,

        @DefaultValue("gpt-5-mini")
        String model,

        @DefaultValue("30s")
        Duration timeout,

        @DefaultValue("2")
        int maxRetries
) {
}
