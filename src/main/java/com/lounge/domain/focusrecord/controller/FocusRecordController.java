package com.lounge.domain.focusrecord.controller;

import com.lounge.domain.focusrecord.service.FocusRecordService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "FocusRecord", description = "집중 기록 API")
@RestController
@RequestMapping("/api/focusrecord")
@RequiredArgsConstructor
public class FocusRecordController {

    private final FocusRecordService focusRecordService;
}
