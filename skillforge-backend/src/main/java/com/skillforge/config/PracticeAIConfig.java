package com.skillforge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for the AI-powered Practice recommendation module.
 * Provides a shared {@link RestTemplate} bean used by
 * {@link com.skillforge.domain.practice.recommendation.service.impl.OpenAIQuestionGeneratorService}
 * to call external AI APIs.
 *
 * <p>This class is additive — it does NOT modify any existing configuration.</p>
 */
@Configuration
public class PracticeAIConfig {

    /**
     * Default {@link RestTemplate} for outbound HTTP calls.
     * Spring Boot 3.x does not auto-configure a RestTemplate bean, so we
     * register one here for use by the AI question generator.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
