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

package com.broadcom.tanzu.demos.springai101.weather;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class WeatherV3Controller {
    private final ChatClient chatClient;

    WeatherV3Controller(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping(value = "/weather/v3", produces = MediaType.TEXT_PLAIN_VALUE)
    String weather(@RequestParam("q") String query) {
        // Use this endpoint to query your LLM with any requests.
        // Whenever you ask for weather information from one or more cities, functions are automatically called as needed.
        return chatClient.prompt()
                .user(query)
                .functions(WeatherFunctions.GET_WEATHER_BY_CITY, WeatherFunctions.GET_WEATHER_BY_CITIES)
                .call()
                .content();
    }
}
