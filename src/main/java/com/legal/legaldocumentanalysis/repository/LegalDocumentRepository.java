package com.legal.legaldocumentanalysis.repository;
import java.util.Optional;
import com.legal.legaldocumentanalysis.model.LegalDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LegalDocumentRepository extends
        JpaRepository<LegalDocument, Long> {

    Optional<LegalDocument> findByFileHash(String fileHash);
}