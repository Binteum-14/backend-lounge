package com.lounge.domain.focusrecord.repository;

import com.lounge.domain.focusrecord.entity.FocusRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FocusRecordRepository extends JpaRepository<FocusRecord, Long> {

    void deleteByUser_Id(Long userId);

    @EntityGraph(attributePaths = "flightFocusDetail")
    List<FocusRecord> findByUser_IdOrderByIdDesc(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = "flightFocusDetail")
    List<FocusRecord> findByUser_IdAndIdLessThanOrderByIdDesc(Long userId, Long cursorId, Pageable pageable);
}
