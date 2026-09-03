package se.lexicon.flightbooking_api.assistant.config;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OpenAiProperties.class)
public class OpenAiConfig {

    @Bean
    @ConditionalOnProperty(
            prefix = "openai",
            name = "enabled",
            havingValue = "true"
    )
    public OpenAIClient openAIClient(OpenAiProperties properties) {
        if (properties.apiKey() == null
                || properties.apiKey().isBlank()) {
            throw new IllegalStateException(
                    "OPENAI_API_KEY must be configured when OpenAI is enabled"
            );
        }

        return OpenAIOkHttpClient.builder()
                .apiKey(properties.apiKey())
                .timeout(properties.timeout())
                .maxRetries(properties.maxRetries())
                .build();
    }
}
