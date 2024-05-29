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

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.time.Duration;

@Configuration(proxyBeanMethods = false)
class RestClientConfig {
    @Bean
    RestClientCustomizer restClientBuilderCustomizer() {
        return restClientBuilder -> {
            final var reqFactory = new SimpleClientHttpRequestFactory();
            // Override timeouts for the default RestClient.Builder instance.
            // which is used by Spring AI when sending outbound requests.
            reqFactory.setConnectTimeout(Duration.ofSeconds(10));
            reqFactory.setReadTimeout(Duration.ofSeconds(60));
            restClientBuilder.requestFactory(reqFactory);
        };
    }
}
