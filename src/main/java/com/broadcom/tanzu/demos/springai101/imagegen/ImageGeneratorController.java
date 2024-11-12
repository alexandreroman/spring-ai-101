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

package com.broadcom.tanzu.demos.springai101.imagegen;

import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImageOptionsBuilder;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@ConditionalOnProperty(name = "app.ai-provider", havingValue = "openai")
class ImageGeneratorController {
    private final ImageModel imageModel;

    ImageGeneratorController(ImageModel imageModel) {
        this.imageModel = imageModel;
    }

    @GetMapping(value = "/imagegen")
    String generateImage(@RequestParam(name = "q", defaultValue = "A penguin sitting on a building, cartoon-style", required = false)
                         String query) {
        // Pick a model for generating images.
        final var imageModelName = "dall-e-3";

        // Set instructions for image generation.
        final var opts = ImageOptionsBuilder.builder()
                .withModel(imageModelName)
                .withWidth(1024)
                .withHeight(1024)
                .build();

        // Let's generate an image!
        final var imagePrompt = new ImagePrompt(query, opts);
        final var resp = imageModel.call(imagePrompt);
        final var imageUrl = resp.getResult().getOutput().getUrl();

        // Redirect this request to the generated image.
        return "redirect:" + imageUrl;
    }
}
