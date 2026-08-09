package com.lounge.domain.visitpassproduct.service;

import com.lounge.domain.visitpassproduct.repository.VisitPassProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VisitPassProductService {

    private final VisitPassProductRepository visitPassProductRepository;
}
