
package com.legal.legaldocumentanalysis.service;

import com.legal.legaldocumentanalysis.model.DocumentChunk;
import com.legal.legaldocumentanalysis.repository.DocumentChunkRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RagRetrievalService {

    private final DocumentChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;

    public RagRetrievalService(
            DocumentChunkRepository chunkRepository,
            EmbeddingService embeddingService) {
        this.chunkRepository = chunkRepository;
        this.embeddingService = embeddingService;
    }

    public List<DocumentChunk> retrieveRelevantChunks(
            Long documentId, String question, int topK) {

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "Please enter a question.");
        }

        List<DocumentChunk> chunks =
                chunkRepository.findByDocumentIdOrderByChunkIndexAsc(
                        documentId);

        if (chunks.isEmpty()) {
            throw new IllegalArgumentException(
                    "No chunks found for this document. "
                            + "Please upload the document again.");
        }

        // Generate an embedding for the user's question.
        List<Double> questionEmbedding =
                embeddingService.generateEmbedding(question);

        // Score every chunk against the question embedding.
        List<ScoredChunk> scoredChunks = new ArrayList<>();

        for (DocumentChunk chunk : chunks) {
            if (chunk.getEmbedding() == null
                    || chunk.getEmbedding().isBlank()) {
                continue;
            }

            List<Double> chunkEmbedding =
                    embeddingService.embeddingFromJson(
                            chunk.getEmbedding());

            double similarity = cosineSimilarity(
                    questionEmbedding, chunkEmbedding);

            scoredChunks.add(
                    new ScoredChunk(chunk, similarity));
        }

        if (scoredChunks.isEmpty()) {
            throw new IllegalArgumentException(
                    "No valid embeddings found for this document.");
        }

        scoredChunks.sort(
                Comparator.comparingDouble(ScoredChunk::score)
                        .reversed());

        int resultCount = Math.min(
                Math.max(topK, 1), scoredChunks.size());

        return scoredChunks.subList(0, resultCount)
                .stream()
                .map(ScoredChunk::chunk)
                .toList();
    }

    private double cosineSimilarity(
            List<Double> first, List<Double> second) {

        if (first.size() != second.size() || first.isEmpty()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double firstMagnitude = 0.0;
        double secondMagnitude = 0.0;

        for (int i = 0; i < first.size(); i++) {
            double a = first.get(i);
            double b = second.get(i);

            dotProduct += a * b;
            firstMagnitude += a * a;
            secondMagnitude += b * b;
        }

        if (firstMagnitude == 0.0 || secondMagnitude == 0.0) {
            return 0.0;
        }

        return dotProduct /
                (Math.sqrt(firstMagnitude)
                        * Math.sqrt(secondMagnitude));
    }

    private record ScoredChunk(
            DocumentChunk chunk, double score) {
    }
}
