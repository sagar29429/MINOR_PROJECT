package com.legal.legaldocumentanalysis.repository;

import com.legal.legaldocumentanalysis.model.LegalDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LegalDocumentRepository extends
        JpaRepository<LegalDocument, Long> {
}