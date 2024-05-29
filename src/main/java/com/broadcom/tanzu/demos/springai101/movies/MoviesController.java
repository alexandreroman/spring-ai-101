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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
class MoviesController {
    private final Logger logger = LoggerFactory.getLogger(MoviesController.class);
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    MoviesController(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
    }

    private static String toDisplayLanguage(String lang) {
        final var displayLang = Locale.forLanguageTag(lang).getDisplayLanguage(Locale.ENGLISH);
        if (displayLang == null || displayLang.isEmpty()) {
            return "english";
        }
        return displayLang;
    }

    @GetMapping(value = "/movies", produces = MediaType.APPLICATION_JSON_VALUE)
    MovieMashupResponse movieMashup(@RequestParam("titles") String[] titles,
                                    @RequestParam(name = "genre", defaultValue = "comedy") String genre,
                                    @RequestParam(name = "lang", defaultValue = "en") String lang) {
        // Look for additional data.
        final var moviesById = new HashMap<String, Document>(2);
        for (final String title : titles) {
            logger.info("Looking up movies with title: {}", title);

            final var docs = vectorStore.similaritySearch(
                    SearchRequest.query(title).withTopK(1).withSimilarityThreshold(0.2));

            logger.info("Found {} movie(s) in the vector store for title {}", docs.size(), title);
            docs.forEach(m -> moviesById.put(m.getId(), m));
        }

        logger.info("Generating a new movie of genre {} using {} movie(s) as sources", genre, moviesById.size());
        final var movies = moviesById.values().stream().map(Document::getContent).collect(Collectors.joining("\n"));
        return chatClient.prompt()
                // Let's build a prompt which is augmented with additional data:
                // this is what Retrieval Augmented Generation (RAG) is all about.
                .user(p -> p.text("""
                                Using only entries from the section SOURCES,
                                create a movie overview for a new movie of genre {genre}.
                                Generate a new title for this new movie.

                                Translate the new movie overview to {lang}.

                                SOURCES
                                ---
                                {movies}
                                """)
                        .param("lang", toDisplayLanguage(lang))
                        .param("genre", genre)
                        .param("movies", movies))
                .call()
                .entity(MovieMashupResponse.class);
    }

    record MovieMashupResponse(
            NewMovie newMovie,
            MovieSource[] sources
    ) {
        record NewMovie(
                String title,
                String overview,
                String genre,
                String language
        ) {
        }

        record MovieSource(
                String title,
                String overview,
                String[] genres
        ) {
        }
    }
}
