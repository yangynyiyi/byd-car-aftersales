package com.byd.car.agent.client;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class AiAgentClient {

    private static final Logger log = LoggerFactory.getLogger(AiAgentClient.class);

    @Value("${ai.agent.api-key}")
    private String apiKey;

    @Value("${ai.agent.base-url}")
    private String baseUrl;

    @Value("${ai.agent.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    public String call(String systemPrompt, String userMessage) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)
                ),
                "temperature", 0.3,
                "max_tokens", 1024
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            log.info("开始调用 Agent: model={}", model);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/chat/completions", request, String.class);

            JSONObject json = new JSONObject(response.getBody());
            String content = json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
            log.info("Agent 调用成功, 返回长度: {}", content.length());
            return content;

        } catch (Exception e) {
            log.error("Agent 调用失败: {}", e.getMessage(), e);
            throw new RuntimeException("AI Agent 调用失败，请稍后重试: " + e.getMessage());
        }
    }
}
