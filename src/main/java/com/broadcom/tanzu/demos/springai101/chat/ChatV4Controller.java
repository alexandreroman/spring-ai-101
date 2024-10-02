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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
class ChatV4Controller {
    private final ChatClient chatClient;

    @Value("classpath:/user-chat.st")
    private Resource userText;

    @Value("classpath:/system-chat.st")
    private Resource sysText;

    ChatV4Controller(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping(value = "/chat/v4", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_XML_VALUE})
    Result chat() {
        var text = """
                Analyze this text and split it into Nodes and Relationships :
                Marie Curie, born in 1867, was a Polish and naturalised-French physicist and chemist who conducted pioneering research on radioactivity.
                She was the first woman to win a Nobel Prize, the first person to win a Nobel Prize twice, and the only person to win a Nobel Prize in two scientific fields.
                Her husband, Pierre Curie, was a co-winner of her first Nobel Prize, making them the first-ever married couple to win the Nobel Prize and launching the Curie family legacy of five Nobel Prizes.
                She was, in 1906, the first woman to become a professor at the University of Paris.
                """;

        return chatClient.prompt()
                .user(p -> p.text(text)).call()
                .entity(Result.class);
    }

    record Node(String id, String type) {
    }

    record Relationship(Node source, Node target, String type) {
    }

    record Result(List<Node> nodes, List<Relationship> relationships) {
    }
}
