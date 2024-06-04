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

package com.broadcom.tanzu.demos.springai101.hello;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.RequestResponseAdvisor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
class HelloController {
    private final ChatClient chatClient;

    HelloController(final ChatClient.Builder chatClientBuilder) {
        chatClient = chatClientBuilder.build();
    }

    @GetMapping(value = "/hello", produces = MediaType.TEXT_PLAIN_VALUE)
    CharSequence hello(@RequestParam(name = "n", defaultValue = "John Doe") String name) {
        // In this example, you can see that the AI engine has no "session" or memory of past conversations,
        // as RequestResponseAdvisor is provided.
        return chatWithAI(name, List.of());
    }

    @GetMapping(value = "/hello-memory", produces = MediaType.TEXT_PLAIN_VALUE)
    CharSequence helloMemory(@RequestParam(name = "n", defaultValue = "John Doe") String name) {
        // Let's bring a MessageChatMemoryAdvisor to start a "real" conversation with the AI engine.
        // Note how the result is different this time.
        return chatWithAI(name, List.of(new MessageChatMemoryAdvisor(new InMemoryChatMemory())));
    }

    private CharSequence chatWithAI(String name, List<RequestResponseAdvisor> advisors) {
        final var buf = new StringBuilder();
        buf.append("Current time is: ").append(Instant.now()).append("\n\n");

        final var p1 = String.format("Hello, my name is %s.", name);
        buf.append("💬️ ").append(p1).append("\n");
        buf.append("🤖 ").append(chatClient.prompt().advisors(advisors).user(p1).call().content()).append("\n\n");

        final var p2 = "Hello, what's my name?";
        buf.append("💬️ ").append(p2).append("\n");
        buf.append("🤖 ").append(chatClient.prompt().advisors(advisors).user(p2).call().content()).append("\n");

        return buf;
    }
}
