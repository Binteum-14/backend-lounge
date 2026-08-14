package com.lounge.domain.visitpass.service;

import com.lounge.domain.visitpass.repository.VisitPassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VisitPassService {

    private final VisitPassRepository visitPassRepository;
}
