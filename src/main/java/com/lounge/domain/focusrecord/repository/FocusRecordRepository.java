package com.lounge.domain.focusrecord.repository;

import com.lounge.domain.focusrecord.entity.FocusRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FocusRecordRepository extends JpaRepository<FocusRecord, Long> {

    void deleteByUser_Id(Long userId);
}
