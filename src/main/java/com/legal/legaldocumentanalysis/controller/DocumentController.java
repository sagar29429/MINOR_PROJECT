package com.legal.legaldocumentanalysis.controller;

import java.util.List;

import com.legal.legaldocumentanalysis.model.LegalDocument;
import com.legal.legaldocumentanalysis.service.LegalDocumentService;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final LegalDocumentService service;

    public DocumentController(LegalDocumentService service) {
        this.service = service;
    }

    @PostMapping("/save")
    public LegalDocument saveDocument(@RequestBody LegalDocument document) {
        return service.saveDocument(document);
    }

    @GetMapping
    public List<LegalDocument> getAllDocuments() {
        return service.getAllDocuments();
    }

    @PostMapping("/upload")
    public LegalDocument uploadDocument(
            @RequestParam("file") MultipartFile file) throws Exception {

        return service.uploadDocument(file);
    }
}