package com.legal.legaldocumentanalysis.service;

import com.legal.legaldocumentanalysis.model.DocumentAnalysis;
import com.legal.legaldocumentanalysis.model.LegalDocument;
import com.legal.legaldocumentanalysis.repository.DocumentAnalysisRepository;
import com.legal.legaldocumentanalysis.repository.LegalDocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DocumentAnalysisService {

    private final DocumentAnalysisRepository analysisRepository;
    private final LegalDocumentRepository documentRepository;
    private final AIAnalysisService aiAnalysisService;

    public DocumentAnalysisService(
            DocumentAnalysisRepository analysisRepository,
            LegalDocumentRepository documentRepository,
            AIAnalysisService aiAnalysisService) {

        this.analysisRepository = analysisRepository;
        this.documentRepository = documentRepository;
        this.aiAnalysisService = aiAnalysisService;
    }

    public DocumentAnalysis analyzeDocument(Long documentId) {

        // Find the legal document
        LegalDocument document = documentRepository.findById(documentId)
                .orElseThrow(() ->
                        new RuntimeException("Document not found with ID: " + documentId));

        String content = document.getContent();

        // Temporary rule-based analysis
        String riskLevel;
        String summary;
        String riskDetails;

        if (content == null || content.trim().isEmpty()) {

            riskLevel = "UNKNOWN";
            summary = "The document has no content to analyze.";
            riskDetails = "No document content was available.";

        } else {

            String lowerContent = content.toLowerCase();

            if (lowerContent.contains("terminate")
                    || lowerContent.contains("penalty")
                    || lowerContent.contains("liable")) {

                riskLevel = "HIGH";
                summary = "The document contains clauses that may require careful legal review.";

                StringBuilder details = new StringBuilder();

                if (lowerContent.contains("terminate")) {
                    details.append("Termination clause detected. ");
                }

                if (lowerContent.contains("penalty")) {
                    details.append("Penalty clause detected. ");
                }

                if (lowerContent.contains("liable")) {
                    details.append("Liability clause detected. ");
                }

                riskDetails = details.toString();

            } else {

                riskLevel = "LOW";
//        analysis.setRiskDetails(riskDetails);
        summary = "No major risk-related keywords were detected in the document.";
        riskDetails = "No major risk clauses detected.";
    }
}

// Create analysis result
DocumentAnalysis analysis = new DocumentAnalysis();

        analysis.setDocumentId(documentId);
        analysis.setRiskLevel(riskLevel);
        analysis.setSummary(summary);

        // Save analysis
        return analysisRepository.save(analysis);
    }

    public DocumentAnalysis analyzeDocumentWithAI(Long documentId) {

        LegalDocument document = documentRepository.findById(documentId)
                .orElseThrow(() ->
                        new RuntimeException("Document not found with ID: " + documentId));

        String content = document.getContent();

        Map<String, String> aiResult =
                aiAnalysisService.analyzeWithAI(content);

        DocumentAnalysis analysis = new DocumentAnalysis();

        analysis.setDocumentId(documentId);
        analysis.setRiskLevel(aiResult.get("riskLevel"));
        analysis.setSummary(aiResult.get("summary"));
        analysis.setRiskDetails(aiResult.get("riskDetails"));

        return analysisRepository.save(analysis);
    }

    public DocumentAnalysis saveAnalysis(DocumentAnalysis analysis) {
        return analysisRepository.save(analysis);
    }

    public List<DocumentAnalysis> getAllAnalyses() {
        return analysisRepository.findAll();
    }
    public List<DocumentAnalysis> getAnalysesByDocument(Long documentId) {
        return analysisRepository.findByDocumentId(documentId);
    }
}