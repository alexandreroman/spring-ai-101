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

package com.broadcom.tanzu.demos.springai101.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ChatV3Controller {
    private final ChatClient chatClient;

    @Value("classpath:/user-chat.st")
    private Resource userText;

    @Value("classpath:/system-chat.st")
    private Resource sysText;

    ChatV3Controller(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping(value = "/chat/v3", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_XML_VALUE})
    JokeResponse chat(@RequestParam("topic") String topic) {
        // Not only we use prompt templating to format the query,
        // but we also convert the response to a Java construct.
        return chatClient.prompt()
                .system(sysText)
                .user(p -> p.text(userText).param("topic", topic))
                .call()
                .entity(JokeResponse.class);
    }

    record JokeResponse(
            String joke,
            String topic
    ) {
    }
}
