package com.legal.legaldocumentanalysis.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import com.legal.legaldocumentanalysis.model.LegalDocument;
import com.legal.legaldocumentanalysis.repository.LegalDocumentRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class LegalDocumentService {

    private final LegalDocumentRepository repository;
    private final PdfTextExtractionService pdfTextExtractionService;
    private final DocumentChunkingService documentChunkingService;

    public LegalDocumentService(
            LegalDocumentRepository repository,
            PdfTextExtractionService pdfTextExtractionService, DocumentChunkingService documentChunkingService) {

        this.repository = repository;
        this.pdfTextExtractionService = pdfTextExtractionService;
        this.documentChunkingService = documentChunkingService;
    }

    public LegalDocument saveDocument(LegalDocument document) {
        document.setUploadedDate(LocalDateTime.now());
        return repository.save(document);
    }

    public List<LegalDocument> getAllDocuments() {
        return repository.findAll();
    }


    private String calculateFileHash(byte[] fileBytes) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(fileBytes);

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "Unable to calculate document hash.", e);
        }
    }






    public LegalDocument uploadDocument(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select a non-empty document.");
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException(
                    "The uploaded file must have a filename.");
        }

        // Calculate the SHA-256 hash of the original file.
        byte[] fileBytes = file.getBytes();
        String fileHash = calculateFileHash(fileBytes);

        // Check whether this exact file was already uploaded.
        Optional<LegalDocument> existingDocument =
                repository.findByFileHash(fileHash);

        if (existingDocument.isPresent()) {
            return existingDocument.get();
        }

        // Extract text according to the file type.
        String lowerName = fileName.toLowerCase(java.util.Locale.ROOT);
        String content;

        if (lowerName.endsWith(".pdf")) {
            content = pdfTextExtractionService.extractText(fileBytes);
        } else if (lowerName.endsWith(".txt")) {
            content = new String(
                    fileBytes,
                    java.nio.charset.StandardCharsets.UTF_8);
        } else {
            throw new IllegalArgumentException(
                    "Only PDF and TXT files are supported.");
        }

        if (content.isBlank()) {
            throw new IllegalArgumentException(
                    "No text could be extracted from the document.");
        }

        LegalDocument document = new LegalDocument();
        document.setFileName(fileName);
        document.setTitle(fileName);
        document.setContent(content);
        document.setUploadedDate(LocalDateTime.now());
        document.setFileHash(fileHash);

        LegalDocument savedDocument = repository.save(document);

        // Generate chunks and embeddings for the new document.
        documentChunkingService.processDocument(
                savedDocument.getId(),
                savedDocument.getContent());

        return savedDocument;
    }



}