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

package com.broadcom.tanzu.demos.springai101.movies;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.List;

@Component
class MovieDatasetLoader {
    private final Logger logger = LoggerFactory.getLogger(MovieDatasetLoader.class);
    private final AsyncTaskExecutor taskExecutor;
    private final ObservationRegistry observationRegistry;
    private final List<MovieProcessor> movieProcessors;

    @Value("classpath:/movies-2024.tsv")
    private Resource moviesRes;

    MovieDatasetLoader(AsyncTaskExecutor taskExecutor, ObservationRegistry observationRegistry, List<MovieProcessor> movieProcessors) {
        this.taskExecutor = taskExecutor;
        this.observationRegistry = observationRegistry;
        this.movieProcessors = movieProcessors;
    }

    public int load() throws IOException {
        return Observation.createNotStarted("load-movies", observationRegistry)
                .observeChecked(this::doLoad);
    }

    private int doLoad() throws IOException {
        logger.debug("Loading movies from CSV resource: {}", moviesRes.getURL());

        // Parse the CSV resource.
        int count = 0;
        int lineNumber = 1;
        try (final var reader = new CSVReaderBuilder(new InputStreamReader(moviesRes.getInputStream()))
                .withCSVParser(new CSVParserBuilder().withSeparator('\t').build()).withSkipLines(1).build()) {
            for (String[] line; (line = reader.readNext()) != null; ++lineNumber) {
                logger.debug("Parsing line number {}", lineNumber);
                final var overview = line[4];
                if (overview == null || overview.isEmpty()) {
                    // Ignore movies with no overview.
                    continue;
                }

                final var id = line[0];
                final var title = line[1];
                final var genres = line[2].split("-");
                final var releaseDate = LocalDate.parse(line[7]);
                final var credits = line.length <= 15 ? null : line[15].split("-");

                // Create a Movie instance and process it.
                final var movie = new Movie(
                        id, title, genres, releaseDate, overview, credits
                );
                processMovie(movie);

                ++count;
            }
        } catch (CsvException e) {
            throw new IOException("Failed to parse movies resource as CSV", e);
        }
        return count;
    }

    private void processMovie(Movie movie) {
        final Runnable task = () -> {
            // As we're about to process the movie, start a new span to observe this task.
            Observation.createNotStarted("processMovie", observationRegistry)
                    .highCardinalityKeyValue("movie", movie.id())
                    .observe(() -> {
                        logger.debug("Processing movie: {} ({})", movie.id(), movie.title());
                        // Movie processing is actually offloaded.
                        for (final MovieProcessor movieProcessor : movieProcessors) {
                            try {
                                movieProcessor.process(movie);
                            } catch (Exception e) {
                                logger.warn("Failed to process movie: {}", movie, e);
                            }
                        }
                    });
        };
        // Asynchronously process the movie.
        taskExecutor.submit(task);
    }
}
