package com.legal.legaldocumentanalysis.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AIAnalysisService {

    private final RestTemplate restTemplate = new RestTemplate();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, String> analyzeWithAI(String content) {

        String url = "http://localhost:11434/api/generate";

        String prompt = """
                You are an intelligent legal document analysis assistant.

                Analyze the following legal document.

                Return ONLY valid JSON in exactly this format:

                {
                  "riskLevel": "LOW",
                  "summary": "Short summary of the document",
                  "riskDetails": "Important legal risks found in the document"
                }

                Rules:
                - riskLevel must be only LOW, MEDIUM, or HIGH.
                - summary must be a short explanation.
                - riskDetails must describe the important risks.
                - Do not add markdown.
                - Do not add ```json.
                - Return only the JSON object.

                Legal Document:
                %s
                """.formatted(content);

        Map<String, Object> request = new HashMap<>();

        request.put("model", "llama3.2");
        request.put("prompt", prompt);
        request.put("stream", false);

        Map<String, Object> response =
                restTemplate.postForObject(url, request, Map.class);

        if (response != null && response.get("response") != null) {

            String aiResponse = response.get("response").toString();

            try {

                return objectMapper.readValue(
                        aiResponse,
                        new TypeReference<Map<String, String>>() {}
                );

            } catch (Exception e) {

                throw new RuntimeException(
                        "Failed to parse AI response: " + aiResponse, e);
            }
        }

        throw new RuntimeException("AI analysis failed.");
    }
}