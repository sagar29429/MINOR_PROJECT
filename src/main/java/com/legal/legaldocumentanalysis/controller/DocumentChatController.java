
package com.legal.legaldocumentanalysis.controller;

import com.legal.legaldocumentanalysis.model.DocumentChunk;
import com.legal.legaldocumentanalysis.service.DocumentChatService;
import com.legal.legaldocumentanalysis.service.RagRetrievalService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class DocumentChatController {

    private final DocumentChatService chatService;
    private final RagRetrievalService ragRetrievalService;

    public DocumentChatController(
            DocumentChatService chatService,
            RagRetrievalService ragRetrievalService) {
        this.chatService = chatService;
        this.ragRetrievalService = ragRetrievalService;
    }

    @PostMapping("/{documentId}")
    public Map<String, Object> askQuestion(
            @PathVariable Long documentId,
            @RequestBody Map<String, String> request) {

        String question = request.get("question");

        if (question == null || question.isBlank()) {
            throw new ChatQuestionException(
                    "Please enter a question.");
        }

        // Generate the chatbot answer.
        String answer =
                chatService.askQuestion(documentId, question);

        // Retrieve the source excerpts for display.
        List<Map<String, Object>> sources =
                ragRetrievalService.retrieveRelevantChunks(
                                documentId, question, 3)
                        .stream()
                        .map(chunk -> Map.<String, Object>of(
                                "chunkIndex", chunk.getChunkIndex(),
                                "content", chunk.getContent()))
                        .toList();

        return Map.of(
                "answer", answer,
                "sources", sources
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class ChatQuestionException
            extends RuntimeException {

        public ChatQuestionException(String message) {
            super(message);
        }
    }
}
