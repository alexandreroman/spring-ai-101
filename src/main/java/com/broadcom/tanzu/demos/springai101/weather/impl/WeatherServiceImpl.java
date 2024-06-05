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
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.stereotype.Service;

@Service
class WeatherServiceImpl implements WeatherService {
    private final WeatherApi api;
    private final ObservationRegistry observationRegistry;

    WeatherServiceImpl(WeatherApi api, ObservationRegistry observationRegistry) {
        this.api = api;
        this.observationRegistry = observationRegistry;
    }

    @Override
    public Weather getWeatherByCity(String city) {
        // Observe service calls using Micrometer API.
        return Observation.createNotStarted("getWeatherByCity", observationRegistry)
                .highCardinalityKeyValue("city", city)
                .observe(() -> doGetWeatherByCity(city));
    }

    private Weather doGetWeatherByCity(String city) {
        return new Weather(city, api.getWeather(city, "metric").details().temperature());
    }
}
