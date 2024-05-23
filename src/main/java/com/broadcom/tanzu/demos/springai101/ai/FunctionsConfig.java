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

package com.broadcom.tanzu.demos.springai101.ai;

import com.broadcom.tanzu.demos.springai101.weather.Weather;
import com.broadcom.tanzu.demos.springai101.weather.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Configuration(proxyBeanMethods = false)
class FunctionsConfig {
    private final Logger logger = LoggerFactory.getLogger(FunctionsConfig.class);

    @Bean
    @Description("Get the current weather in a given city, including temperature (in Celsius)")
    Function<ByCityRequest, Weather> getWeatherByCity(WeatherService weatherService) {
        return req -> {
            logger.info("Loading weather in {} using OpenWeatherMap", req.city());
            return weatherService.getWeatherByCity(req.city());
        };
    }

    record ByCityRequest(String city) {
    }
}
