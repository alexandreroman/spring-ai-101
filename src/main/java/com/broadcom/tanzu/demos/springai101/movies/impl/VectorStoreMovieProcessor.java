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

package com.broadcom.tanzu.demos.springai101.movies.impl;

import com.broadcom.tanzu.demos.springai101.movies.Movie;
import com.broadcom.tanzu.demos.springai101.movies.MovieProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
class VectorStoreMovieProcessor implements MovieProcessor {
    private final Logger logger = LoggerFactory.getLogger(VectorStoreMovieProcessor.class);
    private final VectorStore vectorStore;

    VectorStoreMovieProcessor(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void process(Movie movie) {
        logger.debug("Inserting movie into vector store: {} ({})", movie.id(), movie.title());

        // Create text content for this Movie instance.
        final var movieContentTpl = """
                Id: %s
                Title: %s
                Genres: %s
                Overview: %s
                Released: %s
                Credits: %s
                """;
        final var releaseDateStr = movie.releaseDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        final var movieContent = String.format(movieContentTpl,
                movie.id(), movie.title(),
                String.join(", ", movie.genres()),
                movie.overview(), releaseDateStr,
                movie.credits() == null ? "" : String.join(", ", movie.credits()));

        // Set content metadata.
        final Map<String, Object> metadata = Map.of(
                "title", movie.title(),
                "releaseDate", releaseDateStr,
                "releaseYear", movie.releaseDate().getYear()
        );

        // Build a Spring AI Document which holds a summary of the content we want to index.
        final Document doc = new Document(movie.id(), movieContent, metadata);

        // Let's add this content to the vector store.
        // At this stage, we don't know the actual implementation.
        vectorStore.add(List.of(doc));
    }
}
