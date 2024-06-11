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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Function;

@RestController
class WeatherV1Controller {
    private final Logger logger = LoggerFactory.getLogger(WeatherV1Controller.class);
    private final WeatherService weatherService;
    private final ChatClient chatClient;

    WeatherV1Controller(WeatherService weatherService, ChatClient.Builder chatClientBuilder) {
        this.weatherService = weatherService;
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping(value = "/weather/v1", produces = MediaType.TEXT_PLAIN_VALUE)
    String weather(@RequestParam("city") String city) {
        // Rely on a function to get additional (live) data.
        return chatClient.prompt()
                .user(p -> p.text("What is the current temperature in {city}?").param("city", city))
                .function("getWeatherByCity",
                        "Get the current weather in a given city, including temperature (in Celsius).",
                        new Function<ByCityRequest, Weather>() {
                            @Override
                            public Weather apply(ByCityRequest req) {
                                logger.info("Loading weather from {} using OpenWeatherMap in inline function", req.city());
                                return weatherService.getWeatherByCity(req.city());
                            }
                        })
                .call()
                .content();
    }

    record ByCityRequest(String city) {
    }
}
