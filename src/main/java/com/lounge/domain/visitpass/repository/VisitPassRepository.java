package com.lounge.domain.visitpass.repository;

import com.lounge.domain.visitpass.entity.VisitPass;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitPassRepository extends JpaRepository<VisitPass, Long> {
}
