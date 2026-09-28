package com.legal.legaldocumentanalysis.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import com.legal.legaldocumentanalysis.model.LegalDocument;
import com.legal.legaldocumentanalysis.repository.LegalDocumentRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LegalDocumentService {

    private final LegalDocumentRepository repository;
    private final PdfTextExtractionService pdfTextExtractionService;

    public LegalDocumentService(
            LegalDocumentRepository repository,
            PdfTextExtractionService pdfTextExtractionService) {

        this.repository = repository;
        this.pdfTextExtractionService = pdfTextExtractionService;
    }

    public LegalDocument saveDocument(LegalDocument document) {
        document.setUploadedDate(LocalDateTime.now());
        return repository.save(document);
    }

    public List<LegalDocument> getAllDocuments() {
        return repository.findAll();
    }

    public LegalDocument uploadDocument(MultipartFile file) throws IOException {

        String content = pdfTextExtractionService.extractText(file.getBytes());

        LegalDocument document = new LegalDocument();

        document.setFileName(file.getOriginalFilename());
        document.setTitle(file.getOriginalFilename());
        document.setContent(content);
        document.setUploadedDate(LocalDateTime.now());

        return repository.save(document);
    }
}