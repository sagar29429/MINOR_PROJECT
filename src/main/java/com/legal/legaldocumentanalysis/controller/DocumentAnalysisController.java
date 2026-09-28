package com.legal.legaldocumentanalysis.controller;

import com.legal.legaldocumentanalysis.model.DocumentAnalysis;
import com.legal.legaldocumentanalysis.service.DocumentAnalysisService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analysis")
public class DocumentAnalysisController {

    private final DocumentAnalysisService service;

    public DocumentAnalysisController(DocumentAnalysisService service) {
        this.service = service;
    }

    @PostMapping("/save")
    public DocumentAnalysis saveAnalysis(@RequestBody DocumentAnalysis analysis) {
        return service.saveAnalysis(analysis);
    }

    @GetMapping
    public List<DocumentAnalysis> getAllAnalyses() {
        return service.getAllAnalyses();
    }

    @GetMapping("/document/{documentId}")
    public List<DocumentAnalysis> getAnalysesByDocument(@PathVariable Long documentId) {
        return service.getAnalysesByDocument(documentId);
    }

    @PostMapping("/analyze/{documentId}")
    public DocumentAnalysis analyzeDocument(@PathVariable Long documentId) {
        return service.analyzeDocument(documentId);
    }

    @PostMapping("/ai-analyze/{documentId}")
    public DocumentAnalysis analyzeDocumentWithAI(@PathVariable Long documentId) {
        return service.analyzeDocumentWithAI(documentId);
    }
}