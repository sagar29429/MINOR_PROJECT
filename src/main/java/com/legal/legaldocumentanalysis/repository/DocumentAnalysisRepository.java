package com.legal.legaldocumentanalysis.repository;

import com.legal.legaldocumentanalysis.model.DocumentAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentAnalysisRepository extends JpaRepository<DocumentAnalysis, Long> {

    List<DocumentAnalysis> findByDocumentId(Long documentId);
}