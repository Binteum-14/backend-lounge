package com.lounge.domain.focusrecord.service;

import com.lounge.domain.flightfocusdetail.entity.FlightFocusDetail;
import com.lounge.domain.flightfocusdetail.repository.FlightFocusDetailRepository;
import com.lounge.domain.focusrecord.dto.request.FocusPassSaveRequest;
import com.lounge.domain.focusrecord.dto.response.FocusPassListResponse;
import com.lounge.domain.focusrecord.dto.response.FocusPassResponse;
import com.lounge.domain.focusrecord.dto.response.FocusPassSaveResponse;
import com.lounge.domain.focusrecord.entity.FocusRecord;
import com.lounge.domain.focusrecord.entity.FocusThemeType;
import com.lounge.domain.focusrecord.exception.code.FocusRecordErrorCode;
import com.lounge.domain.focusrecord.repository.FocusRecordRepository;
import com.lounge.domain.user.entity.User;
import com.lounge.domain.user.repository.UserRepository;
import com.lounge.global.api.code.GeneralErrorCode;
import com.lounge.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FocusRecordService {

    private final FocusRecordRepository focusRecordRepository;
    private final FlightFocusDetailRepository flightFocusDetailRepository;
    private final UserRepository userRepository;

    public FocusPassListResponse getFocusPasses(Long userId, Long cursorId, int size) {
        Pageable pageable = PageRequest.of(0, size + 1);
        List<FocusRecord> focusRecords = cursorId == null
                ? focusRecordRepository.findByUser_IdOrderByIdDesc(userId, pageable)
                : focusRecordRepository.findByUser_IdAndIdLessThanOrderByIdDesc(userId, cursorId, pageable);

        boolean hasNext = focusRecords.size() > size;
        List<FocusRecord> currentPage = hasNext ? focusRecords.subList(0, size) : focusRecords;
        List<FocusPassResponse> items = currentPage.stream()
                .map(FocusPassResponse::from)
                .toList();
        Long nextCursorId = hasNext && !currentPage.isEmpty()
                ? currentPage.get(currentPage.size() - 1).getId()
                : null;

        return FocusPassListResponse.of(items, nextCursorId, hasNext);
    }

    @Transactional
    public FocusPassSaveResponse saveFocusPass(Long userId, FocusPassSaveRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));

        FocusRecord focusRecord = FocusRecord.create(
                user,
                request.getThemeType(),
                request.getAllMinutes(),
                request.getStudySeconds(),
                request.getStartedAt(),
                request.getEndedAt()
        );
        FocusRecord savedFocusRecord = focusRecordRepository.save(focusRecord);

        if (request.getThemeType() == FocusThemeType.FLIGHT) {
            saveFlightFocusDetail(savedFocusRecord, request);
        }

        return FocusPassSaveResponse.of(savedFocusRecord.getId());
    }

    private void saveFlightFocusDetail(FocusRecord savedFocusRecord, FocusPassSaveRequest request) {
        if (!StringUtils.hasText(request.getFlightNumber())
                || !StringUtils.hasText(request.getDepartureAirport())
                || !StringUtils.hasText(request.getArrivalAirport())
                || request.getDepartureTime() == null) {
            throw GeneralException.of(FocusRecordErrorCode.FLIGHT_INFO_REQUIRED);
        }

        LocalDateTime departureAt = request.getStartedAt().toLocalDate().atTime(request.getDepartureTime());
        FlightFocusDetail flightFocusDetail = FlightFocusDetail.create(
                savedFocusRecord,
                request.getFlightNumber(),
                request.getDepartureAirport(),
                request.getArrivalAirport(),
                departureAt,
                null
        );
        flightFocusDetailRepository.save(flightFocusDetail);
    }
}
