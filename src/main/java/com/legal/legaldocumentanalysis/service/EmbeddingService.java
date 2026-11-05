
package com.legal.legaldocumentanalysis.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Double> generateEmbedding(String text) {

        String url = "http://localhost:11434/api/embed";

        Map<String, Object> request = Map.of(
                "model", "nomic-embed-text",
                "input", text
        );

        Map<?, ?> response = restTemplate.postForObject(
                url, request, Map.class);

        if (response == null || !(response.get("embeddings") instanceof List<?> vectors)
                || vectors.isEmpty()
                || !(vectors.get(0) instanceof List<?> values)) {
            throw new RuntimeException(
                    "Ollama did not return an embedding.");
        }

        List<Double> embedding = new ArrayList<>();

        for (Object value : values) {
            if (!(value instanceof Number number)) {
                throw new RuntimeException(
                        "Invalid value in embedding response.");
            }
            embedding.add(number.doubleValue());
        }

        return embedding;
    }


    public String embeddingToJson(List<Double> embedding) {
        try {
            return objectMapper.writeValueAsString(embedding);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not serialize embedding.", e);
        }
    }

    public List<Double> embeddingFromJson(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            List<Double> embedding = new ArrayList<>();

            for (JsonNode value : node) {
                embedding.add(value.asDouble());
            }

            return embedding;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not read stored embedding.", e);
        }
    }
}
