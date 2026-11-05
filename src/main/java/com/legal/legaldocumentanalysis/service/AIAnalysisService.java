
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

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                    "The document contains no text to analyze.");
        }

        String url = "http://localhost:11434/api/generate";

        // Limit the input size to avoid excessively large prompts.
        int maxCharacters = 12000;
        String documentText = content.length() > maxCharacters
                ? content.substring(0, maxCharacters)
                : content;

        String prompt = """
                You are a legal document analysis assistant.

                Analyze the supplied legal document excerpt.

                Return exactly one valid JSON object with these fields:
                {
                  "riskLevel": "LOW",
                  "summary": "Short summary of the document",
                  "riskDetails": "Important legal risks found in the document"
                }

                Rules:
                - riskLevel must be exactly LOW, MEDIUM, or HIGH.
                - Include all three fields.
                - Keep summary concise.
                - Explain risks only when supported by the document.
                - If information is insufficient, state that clearly.
                - Do not invent legal facts.
                - Do not include Markdown or code fences.
                - Escape quotation marks inside JSON string values.
                - Return no text before or after the JSON object.

                Legal Document:
                %s
                """.formatted(documentText);

        Map<String, Object> request = new HashMap<>();
        request.put("model", "llama3.2");
        request.put("prompt", prompt);
        request.put("stream", false);
        request.put("format", "json");
        request.put("keep_alive", "10m");

        Map<String, Object> options = new HashMap<>();
        options.put("temperature", 0.1);
        options.put("num_predict", 600);
        request.put("options", options);

        Map<?, ?> response = restTemplate.postForObject(
                url, request, Map.class);

        if (response == null || response.get("response") == null) {
            throw new RuntimeException(
                    "Ollama returned an empty AI analysis response.");
        }

        String aiResponse = response.get("response").toString().trim();

        try {
            Map<String, String> result = objectMapper.readValue(
                    aiResponse,
                    new TypeReference<Map<String, String>>() {}
            );

            String riskLevel = result.get("riskLevel");
            String summary = result.get("summary");
            String riskDetails = result.get("riskDetails");

            if (riskLevel == null || summary == null
                    || riskDetails == null) {
                throw new RuntimeException(
                        "AI response is missing required fields.");
            }

            riskLevel = riskLevel.trim().toUpperCase();

            if (!riskLevel.equals("LOW")
                    && !riskLevel.equals("MEDIUM")
                    && !riskLevel.equals("HIGH")) {
                riskLevel = "MEDIUM";
            }

            result.put("riskLevel", riskLevel);
            return result;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not parse AI response. Raw response: "
                            + aiResponse, e);
        }
    }
}
