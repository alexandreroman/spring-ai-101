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

package com.broadcom.tanzu.demos.springai101.weather.impl;

import com.broadcom.tanzu.demos.springai101.weather.Weather;
import com.broadcom.tanzu.demos.springai101.weather.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.core.task.AsyncTaskExecutor;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration(proxyBeanMethods = false)
class WeatherFunctionsConfig {
    private final Logger logger = LoggerFactory.getLogger(WeatherFunctionsConfig.class);

    @Bean
    @Description("""
            Get the current weather in a given city, including temperature (in Celsius).
            Call this function if you need to get the weather in a single city.
            """)
    Function<ByCityRequest, Weather> getWeatherByCity(WeatherService weatherService) {
        // Map a Spring AI function (including description which will be used by the LLM) to your business function.
        return req -> {
            logger.info("Loading weather from {} using OpenWeatherMap", req.city());
            return weatherService.getWeatherByCity(req.city());
        };
    }

    @Bean
    @Description("""
            Get the current weather in different cities, all at once.
            The result is a map of weather details (including temperature in Celsius) by city.
            Call this function to optimize calls when you need to get the weather in different cities.
            """)
    Function<ByCitiesRequest, Map<String, Weather>> getWeatherByCities(WeatherService weatherService, AsyncTaskExecutor taskExecutor) {
        return req -> {
            if (logger.isInfoEnabled()) {
                final var citiesStr = String.join(", ", req.cities());
                logger.info("Loading weather from different cities ({}) using OpenWeatherMap", citiesStr);
            }

            final var tasks = new ArrayList<CompletableFuture<Weather>>(req.cities().length);
            for (final var city : req.cities()) {
                final var task = taskExecutor.submitCompletable(() -> {
                    logger.debug("Asynchronously loading weather from {} using OpenWeatherMap", city);
                    return weatherService.getWeatherByCity(city);
                });
                tasks.add(task);
            }

            return tasks.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toMap(Weather::city, Function.identity()));
        };
    }

    /**
     * A record holding a city based request when using functions.
     */
    record ByCityRequest(String city) {
    }

    /**
     * A record holding cities.
     */
    record ByCitiesRequest(String[] cities) {
    }
}
