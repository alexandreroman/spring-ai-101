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
class WeatherV2Controller {
    private final ChatClient chatClient;

    WeatherV2Controller(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping(value = "/weather/v2", produces = MediaType.APPLICATION_JSON_VALUE)
    TemperatureResponse weather(@RequestParam("city") String city) {
        // Note the use of entity() to map the response to a Java construct that you can use in your app.
        // This allows you to use AI generated responses without having to manually parse the content.
        return chatClient.prompt()
                .user(p -> p.text("What is the current temperature in {city}?").param("city", city))
                .functions(WeatherFunctions.GET_WEATHER_BY_CITY)
                .call()
                .entity(TemperatureResponse.class);
    }

    record TemperatureResponse(String city, float temperature) {
    }
}
