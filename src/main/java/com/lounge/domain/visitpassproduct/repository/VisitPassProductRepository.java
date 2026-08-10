package com.lounge.domain.visitpassproduct.repository;

import com.lounge.domain.visitpassproduct.entity.VisitPassProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitPassProductRepository extends JpaRepository<VisitPassProduct, Long> {

    void deleteByVisitPass_User_Id(Long userId);
}
