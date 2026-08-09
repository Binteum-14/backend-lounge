package com.ddcj.binteum.domain.diagnosis.repository;

import com.ddcj.binteum.domain.diagnosis.entity.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {
}
