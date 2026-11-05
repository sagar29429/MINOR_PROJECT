
package com.legal.legaldocumentanalysis.service;

import com.legal.legaldocumentanalysis.model.DocumentChunk;
import com.legal.legaldocumentanalysis.repository.DocumentChunkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentChunkingService {

    private static final int CHUNK_SIZE = 1200;
    private static final int OVERLAP = 200;

    private final DocumentChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;

    public DocumentChunkingService(
            DocumentChunkRepository chunkRepository,
            EmbeddingService embeddingService) {
        this.chunkRepository = chunkRepository;
        this.embeddingService = embeddingService;
    }

    @Transactional
    public int processDocument(Long documentId, String content) {

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                    "Document has no extracted text.");
        }

        // Remove any chunks from a previous processing attempt.
        chunkRepository.deleteByDocumentId(documentId);

        List<String> chunks = splitIntoChunks(content);

        int index = 0;

        for (String chunkText : chunks) {
            List<Double> embedding =
                    embeddingService.generateEmbedding(chunkText);

            DocumentChunk chunk = new DocumentChunk();
            chunk.setDocumentId(documentId);
            chunk.setChunkIndex(index++);
            chunk.setContent(chunkText);
            chunk.setEmbedding(
                    embeddingService.embeddingToJson(embedding));

            chunkRepository.save(chunk);
        }

        return chunks.size();
    }

    private List<String> splitIntoChunks(String text) {

        List<String> chunks = new ArrayList<>();

        String normalized = text.replaceAll("\\s+", " ").trim();

        if (normalized.isEmpty()) {
            return chunks;
        }

        int start = 0;

        while (start < normalized.length()) {
            int end = Math.min(
                    start + CHUNK_SIZE, normalized.length());

            // Prefer ending at a sentence or word boundary.
            if (end < normalized.length()) {
                int sentenceEnd = normalized.lastIndexOf(". ", end);

                if (sentenceEnd > start + CHUNK_SIZE / 2) {
                    end = sentenceEnd + 1;
                } else {
                    int wordEnd = normalized.lastIndexOf(' ', end);

                    if (wordEnd > start) {
                        end = wordEnd;
                    }
                }
            }

            String chunk = normalized.substring(start, end).trim();

            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            if (end >= normalized.length()) {
                break;
            }

            start = Math.max(start + 1, end - OVERLAP);
        }

        return chunks;
    }
}
