/*
 * Copyright (c) 2024 Broadcom, Inc. or its affiliates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.broadcom.tanzu.demos.springai101;

import io.github.bucket4j.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.mistralai.MistralAiChatModel;
import org.springframework.ai.mistralai.MistralAiEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "app.ai-provider", havingValue = "mistralai")
@EnableAutoConfiguration(excludeName = "org.springframework.ai.autoconfigure.azure.openai.AzureOpenAiAutoConfiguration")
class MistralAIConfig {
    private final Logger logger = LoggerFactory.getLogger(MistralAIConfig.class);

    @Bean
    ChatClient.Builder chatClientBuilder(MistralAiChatModel mistralAiChatModel) {
        return ChatClient.builder(mistralAiChatModel);
    }

    @Bean
    @Primary
    EmbeddingModel embeddingModel(MistralAiEmbeddingModel mistralAiEmbeddingModel) {
        return mistralAiEmbeddingModel;
    }

    @Bean
    RestClientCustomizer apiRateLimiter(@Value("${app.mistralai.rps}") int rps) {
        // Mistral AI client has no built-in API rate limiter.
        // Let's bring our own implementation.
        final var bucket = Bucket.builder()
                .addLimit(limit -> limit.capacity(rps).refillGreedy(rps, Duration.ofMillis(1500)))
                .build();
        return restClientBuilder -> {
            restClientBuilder.requestInitializer(request -> {
                try {
                    logger.trace("Applying rate limiter");
                    bucket.asBlocking().consume(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException("Unexpected exception", e);
                }
            });
        };
    }
}
