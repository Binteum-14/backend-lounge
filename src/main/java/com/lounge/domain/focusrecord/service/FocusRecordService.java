package com.lounge.domain.focusrecord.service;

import com.lounge.domain.focusrecord.repository.FocusRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FocusRecordService {

    private final FocusRecordRepository focusRecordRepository;
}
