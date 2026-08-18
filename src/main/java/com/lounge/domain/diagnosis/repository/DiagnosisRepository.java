package com.lounge.domain.diagnosis.repository;

import com.lounge.domain.diagnosis.entity.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {

    Optional<Diagnosis> findByIdAndUser_Id(
            Long diagnosisId,
            Long userId
    );

    @Modifying(clearAutomatically = true)
    @Query("update Diagnosis d set d.user = null where d.user.id = :userId")
    int detachUser(@Param("userId") Long userId);
}