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

package com.broadcom.tanzu.demos.springai101.function;

import com.broadcom.tanzu.demos.springai101.weather.WeatherService;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.Media;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URL;

@RestController
class WeatherV4Controller {
    private final ChatClient chatClient;

    @Value("classpath:/paris.jpg")
    private Resource paris;

    @Value("classpath:/lyon.jpg")
    private Resource lyon;

    WeatherV4Controller(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping(value = "/weather/v4", produces = MediaType.APPLICATION_JSON_VALUE)
    TemperatureResponse weather(@RequestParam("u") URL url) {
        // TODO get the image using the URL parameter
        return chatClient.prompt()
                .user(p -> p.text("""
                        Describe this photo, and find out the city.
                        Then, get the current weather for this city.
                        """).media(new Media(MimeTypeUtils.IMAGE_JPEG, lyon)))
                .functions("getWeatherByCity")
                .call()
                .entity(TemperatureResponse.class);
    }

    record TemperatureResponse(String city, float temperature) {
    }
}
